# IOC容器 - Bean的获取流程

> 本文主要讲的是AbstractBeanFactory类中doGetBean方法的流程。

<!-- more -->

---

[TOC]

## 概述

本文重点是Bean的获取流程，主要的方法就是AbstractBeanFactory#doGetBean。

除了整理清楚Bean的获取流程，也希望能明白，BeanFactory中的各种缓存集合的作用。



## AbstractBeanFactory#doGetBean

```java
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
			@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
    	// 处理Bean名称，去除&前缀以及转化别名为真实名称
		final String beanName = transformedBeanName(name);
		Object bean;

		// Eagerly check singleton cache for manually registered singletons.
    	// 获取Bean，这里主要是从缓存中获取单例的对象
		Object sharedInstance = getSingleton(beanName);
    	// 从缓存中获取到了Bean对象
		if (sharedInstance != null && args == null) {
            	// 日志打印
                if (logger.isTraceEnabled()) {
                    if (isSingletonCurrentlyInCreation(beanName)) {
                        logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
                                "' that is not fully initialized yet - a consequence of a circular reference");
                    } else {
                        logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
                    }
                }
            	// 另外一个获取流程
                bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}else {
			// Fail if we're already creating this bean instance:
			// We're assumably within a circular reference.
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// Check if bean definition exists in this factory.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (parentBeanFactory instanceof AbstractBeanFactory) {
					return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
							nameToLookup, requiredType, args, typeCheckOnly);
				}
				else if (args != null) {
					// Delegation to parent with explicit args.
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else if (requiredType != null) {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
				else {
					return (T) parentBeanFactory.getBean(nameToLookup);
				}
			}

			if (!typeCheckOnly) {
				markBeanAsCreated(beanName);
			}

			try {
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// Guarantee initialization of beans that the current bean depends on.
				String[] dependsOn = mbd.getDependsOn();
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						if (isDependent(beanName, dep)) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
						}
						registerDependentBean(dep, beanName);
						try {
							getBean(dep);
						}
						catch (NoSuchBeanDefinitionException ex) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
						}
					}
				}

				// Create bean instance.
				if (mbd.isSingleton()) {
					sharedInstance = getSingleton(beanName, () -> {
						try {
							return createBean(beanName, mbd, args);
						}
						catch (BeansException ex) {
							// Explicitly remove instance from singleton cache: It might have been put there
							// eagerly by the creation process, to allow for circular reference resolution.
							// Also remove any beans that received a temporary reference to the bean.
							destroySingleton(beanName);
							throw ex;
						}
					});
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}

				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}

				else {
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, () -> {
							beforePrototypeCreation(beanName);
							try {
								return createBean(beanName, mbd, args);
							}
							finally {
								afterPrototypeCreation(beanName);
							}
						});
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new BeanCreationException(beanName,
								"Scope '" + scopeName + "' is not active for the current thread; consider " +
								"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
								ex);
					}
				}
			}
			catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}

		// Check if required type matches the type of the actual bean instance.
		if (requiredType != null && !requiredType.isInstance(bean)) {
			try {
				T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
				if (convertedBean == null) {
					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
				}
				return convertedBean;
			}
			catch (TypeMismatchException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}
```



### #transformedBeanName - 转换Bean名称

**该方法主要为了去除FactoryBean的&的前缀，并获取最原始的Bean名称。**

```java
	protected String transformedBeanName(String name) {
		return canonicalName(BeanFactoryUtils.transformedBeanName(name));
	}
```



#### #transformedBeanName - 去&前缀

```java
// BeanFactory接口中的常量
String FACTORY_BEAN_PREFIX = "&";

public static String transformedBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
    	// 没有前缀则直接返回
        if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            	return name;
        }
        // 递归去除Bean中的前缀
        return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
                do {
                    beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
                } while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
                return beanName;
        });
}
```

该方法就是去除所有前缀FactoryBean的前缀。

并且在transformedBeanNameCache会有一个K/V的缓存，key是为去前缀之前的名字，value是去除前缀之后的名字



#### #canonicalName - 去别名

```java
// SimpleAliasRegistry
public String canonicalName(String name) {
       String canonicalName = name;
       // Handle aliasing...
       String resolvedName;
    	// 循环直到aliasMap中不存在 canonicalName
       do {
           	 //  aliasMap就是一个str/str的Map，保存别名和原始名称的映射关系
              resolvedName = this.aliasMap.get(canonicalName);
              if (resolvedName != null) {
                 	canonicalName = resolvedName;
              }
       } while (resolvedName != null);
       return canonicalName;
}
```

该方法功能就是剥离所有别名，获取原始Bean名称的。

SimpleAliasRegistry中存储别名的方式就是通过新旧Bean名称的映射关系。

有时原始的Bean名称可能也是个别名，所以此处需要while循环到名称不在作为Key存在于aliasMap中。



### #getSingleton - 从缓存中获取Bean

```java
// DefaultSingletonBeanRegistry
public Object getSingleton(String beanName) {
   		return getSingleton(beanName, true);
}

// DefaultSingletonBeanRegistry
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    	// 从singletonObjects中获取bean
        Object singletonObject = this.singletonObjects.get(beanName);
    	// 不存在于singletonObjects中，也不存在于singletonsCurrentlyInCreation中
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            	// 这里就要加锁了
                synchronized (this.singletonObjects) {
                    	// 从earlySingletonObjects中获取
                        singletonObject = this.earlySingletonObjects.get(beanName);
                    	// 没有获取到，并且判断是否允许早起引用
                        if (singletonObject == null && allowEarlyReference) {
                            	// 从singletonFactories中获取该类ObjectFactory。
                                ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                                if (singletonFactory != null) {
                                    	// 创建对象，并将其放入早期单例的集合中，
                                    	// 从单例工厂中移除
                                        singletonObject = singletonFactory.getObject();
                                        this.earlySingletonObjects.put(beanName, singletonObject);
                                        this.singletonFactories.remove(beanName);
                                }
                        }
                }
        }
        return singletonObject;
}
```

该方法是用来获取单例Bean的，从中可以看到获取过程的三级缓存。

1. singletonObjects - 存放完好的单例bean对象
2. earlySingletonObjects - 存放单例对象的早期引用
3. singletonFactories - 存放单例对象的ObjectFactory

在最后创建的时候，Bean对象从singletonFactories中转移到了earlySingletonObjects中。<font size=2>(严格来说singletonFactories存的是ObjectFactory)</font>



#### #isSingletonCurrentlyInCreation - 对象是否正在创建

```java
public boolean isSingletonCurrentlyInCreation(String beanName) {
    	// singletonsCurrentlyInCreation应该就是保存正在创建中的Bean的名称的
		return this.singletonsCurrentlyInCreation.contains(beanName);
}
```

该方法主要用于判断beanName对应的Bean对象是否在创建中。

判断的依据也很简单，就是beanName在不在singletonsCurrentlyInCreation集合中。



### #getObjectForBeanInstance - 从入参的beanInstance中获取对象

入参的name可能带有&前缀，但beanName则是原始Bean的名称。

```java
protected Object getObjectForBeanInstance(
      Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

   // Don't let calling code try to dereference the factory if the bean isn't a factory.
    // 判断name是否是FactoryBean对象
    // 这里的判断逻辑非常简单，就是name是否已&开头
   if (BeanFactoryUtils.isFactoryDereference(name)) {
       	 // NullBean代表什么意义未知
          if (beanInstance instanceof NullBean) {
             	return beanInstance;
          }
       	  // name以&开头但是实例却不是FactoryBean，直接抛出异常
          if (!(beanInstance instanceof FactoryBean)) {
             	throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
          }
       
          // 到这后面的判断是name以&为前缀，且beanInstance为FactoryBean类型 
       
       	 // 配置RootBeanDefinition的FactoryBean属性
          if (mbd != null) {
             	mbd.isFactoryBean = true;
          }
       	  // 直接返回了
          return beanInstance;
   }

   // Now we have the bean instance, which may be a normal bean or a FactoryBean.
   // If it's a FactoryBean, we use it to create a bean instance, unless the
   // caller actually wants a reference to the factory.
    // 到这一步的条件就是name为空或者name不以&开头
    // 并且入参实例不是FactoryBean
   if (!(beanInstance instanceof FactoryBean)) {
      		return beanInstance;
   }
    
    // 在这之前如果是标准的FactoryBean，或者标准的普通Bean都直接返回了。
    // name带有&前缀，且beanInstance为FactoryBean类型
    // beanInstance不为FactoryBean类型就直接返回了
    // 剩下的就是没有&前缀的FactoryBean类型了。

   Object object = null;
   if (mbd != null) {
           // 这里可以确定Bean是FactoryBean了，且名称没有带&前缀
          mbd.isFactoryBean = true;
   } else {
       	// 从factoryBeanObjectCache缓存中获取对象
      	object = getCachedObjectForFactoryBean(beanName);
   }
    // mbd不为null，或者mbd为null，但是没有从缓存中获取到对象
   if (object == null) {
          // Return bean instance from factory.
          FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
          // Caches object obtained from FactoryBean if it is a singleton.
          // 获取合并BeanDefinition
          if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
          }
          boolean synthetic = (mbd != null && mbd.isSynthetic());
          object = getObjectFromFactoryBean(factory, beanName, !synthetic);
   }
   return object;
}
```



#### #getCachedObjectForFactoryBean - 缓存中获取FactoryBean对象

```java
// 继承自FactoryBeanRegistrySupport的方法
@Nullable
protected Object getCachedObjectForFactoryBean(String beanName) {
    	return this.factoryBeanObjectCache.get(beanName);
}
```

取缓存的逻辑好像都很简单，就是从Map里面取。





#### #getMergedLocalBeanDefinition - 获取合成的BeanDefinition

```java
protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        // Quick check on the concurrent map first, with minimal locking.
    	// 从缓存中获取该BeanDefinition
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null && !mbd.stale) {
            	return mbd;
        }
    	// 先从beanDefinitionMap缓存中获取BeanDefinition
    	// 再获取合并的BeanDefinition
        return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
}
```

该方法的作用就是获取一个合并的BeanDefinition，这里的合并是指和父BeanDefinition合并，子BeanDefinition去覆盖父BeanDefinition的一些属性。

获取流程也不复杂：

1. 从缓存中获取已经合并了的RootBeanDefinition，获取到了就直接返回
2. 从beanDefinitionMap中获取BeanDefinition，这步是必须要获取到的，没有则直接报错。
3. 将获取到的BeanDefinition与其父BeanDefinition合并，如果有的话。



##### #getBeanDefinition - 从存量中获取BeanDefinition

```java
// DefaultListableBeanFactory
@Override
public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
       BeanDefinition bd = this.beanDefinitionMap.get(beanName);
       if (bd == null) {
              if (logger.isTraceEnabled()) {
                    logger.trace("No bean named '" + beanName + "' found in " + this);
              }
              throw new NoSuchBeanDefinitionException(beanName);
       }
       return bd;
}
```

该方法就是从BeanDefinition的缓存中根据beanName获取，获取不到就报错。



##### #getMergedBeanDefinition - 获取合并的BeanDefinition

```java
protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
      throws BeanDefinitionStoreException {
   return getMergedBeanDefinition(beanName, bd, null);
}
```

以上是过渡方法，完整逻辑在下面的方法中。

```java
protected RootBeanDefinition getMergedBeanDefinition(
			String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
			throws BeanDefinitionStoreException {
		// 需要对mergedBeanDefinitions对象上锁，
    	// 可以关注一下，接下来mergedBeanDefinitions的作用
		synchronized (this.mergedBeanDefinitions) {
                RootBeanDefinition mbd = null;
                RootBeanDefinition previous = null;

                // Check with full lock now in order to enforce the same merged instance.
                // 从mergedBeanDefinitions中获取BeanDefinition
                if (containingBd == null) {
                        mbd = this.mergedBeanDefinitions.get(beanName);
                }

                // 没有获取到，或者获取到的BeanDefinition是旧的才需要走下面的合并流程
                if (mbd == null || mbd.stale) {
                    	// 如果mbd不为null，但是stale
                    	// 此处的previous就可以在最后做合并。
                        previous = mbd;
                        // parentName为空表示没有父级的BeanDefinition，就直接将复制bd
                        // bd如果是RootBeanDefinition就采用克隆，不然则采用包装
                        if (bd.getParentName() == null) {
                                // Use copy of given root bean definition.
                                if (bd instanceof RootBeanDefinition) {
                                        mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                                } else {
                                        mbd = new RootBeanDefinition(bd);
                                }
                        // 以下是存在父级BeanDefinition的情况
                        } else {
                                // Child bean definition: needs to be merged with parent.
                                // 需要将父级BeanDefinition和子级的合并
                                BeanDefinition pbd;
                                try {
                                    // 上面讲过的方法，去除&前缀，恢复到原始的BeanName
                                    String parentBeanName = transformedBeanName(bd.getParentName());
                                    //  父BeanDefinition的名称和自己是否相同
                                    // 相同则代表父BeanDefinition在父BeanFactory中
                                    if (!beanName.equals(parentBeanName)) {
                                                // 同样的获取方式，因为父级的BeanDefinition可能也有父级
                                                pbd = getMergedBeanDefinition(parentBeanName);
                                    }   else {	
                                            // 获取父级的BeanFactory
                                            BeanFactory parent = getParentBeanFactory();
                                            // 从父级的BeanFactory中获取BeanName
                                            if (parent instanceof ConfigurableBeanFactory) {
                                                    pbd = ((ConfigurableBeanFactory) parent).getMergedBeanDefinition(parentBeanName);
                                            } else {
                                                throw new NoSuchBeanDefinitionException(parentBeanName,
                                                        "Parent name '" + parentBeanName + "' is equal to bean name '" + beanName +
                                                        "': cannot be resolved without a ConfigurableBeanFactory parent");
                                            }
                                    }
                            } catch (NoSuchBeanDefinitionException ex) {
                                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
                                            "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                            }
                            // Deep copy with overridden values.
                            mbd = new RootBeanDefinition(pbd);
                            // 用bd覆盖mbd的内容
                            mbd.overrideFrom(bd);
                        }

                    // Set default singleton scope, if not configured before.
                    if (!StringUtils.hasLength(mbd.getScope())) {
                        mbd.setScope(SCOPE_SINGLETON);
                    }

                    // A bean contained in a non-singleton bean cannot be a singleton itself.
                    // Let's correct this on the fly here, since this might be the result of
                    // parent-child merging for the outer bean, in which case the original inner bean
                    // definition will not have inherited the merged outer bean's singleton status.
                    if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                        mbd.setScope(containingBd.getScope());
                    }

                    // Cache the merged bean definition for the time being
                    // (it might still get re-merged later on in order to pick up metadata changes)
                    // 在containingBd为空的情况下，才会去缓存合成后的Bean对象
                    if (containingBd == null && isCacheBeanMetadata()) {
                            this.mergedBeanDefinitions.put(beanName, mbd);
                    }
                }	
                // 如果旧BeanDefinition不为空
                if (previous != null) {
                        copyRelevantMergedBeanDefinitionCaches(previous, mbd);
                }
                return mbd;
            }
	}
```

该方法主要用户获取合并的BeanDefinition，**合并主要是指子BeanDefinition去覆盖父BeanDefinition的部分属性**，并获取最终对象。

方法的整体逻辑如下：

1. 如果入参containingBd为空，则尝试去缓存中获取目标的BeanDefinition
2. 如果**目标BeanDefinition过时或者并不在缓存中**，进入第3步，否则直接跳到第7步。
3. 判断**入参的BeanDefinition是否有父级**，没有直接跳到第7步。
4. 有的话会先获取BeanDefinition，根据**名称是否一样判断从当前BeanFactory中获取还是从父BeanFactory中获取。**
5. 获取到父级BeanDefinition之后拿入参子BeanDefinition去覆盖一些父类的内容(这里就算合成吧)
6. 设置合成后BeanDefinition的生命周期，并看情况设置缓存。
7. 如果第2步中获取的BeanDefinition是过时而非没获取到，则将其与合成后的BeanDefinition合并。
8. 返回合成后的BeanDefinition

**方法中只有在containingBd为空并且允许缓存的情况下才会去缓存合成的BeanDefinition。**

另外父级的BeanDefinition可能会存在于父级的BeanFactory中。



#### #getObjectFromFactoryBean - 从FactoryBean中获取对象

```java
// FactoryBeanRegistrySupport
protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
		// FactoryBean是单例模式，并且存在于singletonObjects缓存中
    	if (factory.isSingleton() && containsSingleton(beanName)) {
            	// 这里就是对singletonObjects上锁
                synchronized (getSingletonMutex()) {
                    	// 从factoryBeanObjectCache中获取对象，对象不为空就直接
                        Object object = this.factoryBeanObjectCache.get(beanName);
                        if (object == null) {
                            	// 内层包括系统安全的东西有点不明白
                            	// 先简单当做 factory.getObject()
                                object = doGetObjectFromFactoryBean(factory, beanName);
                                // Only post-process and store if not put there already during getObject() call above
                                // (e.g. because of circular reference processing triggered by custom getBean calls)
                            	// 再从缓存中获取一遍
                                Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                            	// 如果已经存在
                                if (alreadyThere != null) {
                                    	object = alreadyThere;
                                } else {
                                    	// 是否需要执行BeanPostProcessor
                                        if (shouldPostProcess) {
                                                if (isSingletonCurrentlyInCreation(beanName)) {
                                                    // Temporarily return non-post-processed object, not storing it yet..	
                                                    // 临时返回，不需要进一步存储。
                                                    return object;
                                                }
                                            	// 执行创建前的准备工作，判断以及将beanName加到singletonsCurrentlyInCreation缓存中
                                                beforeSingletonCreation(beanName);
                                                try {
                                                    // Debug后进入的是AbstractAutowireCapableBeanFactory的方法
                                                    // 遍历调用BeanPostProcesser的后置钩子方法
                                                    object = postProcessObjectFromFactoryBean(object, beanName);
                                                } catch (Throwable ex) {
                                                    throw new BeanCreationException(beanName,
                                                            "Post-processing of FactoryBean's singleton object failed", ex);
                                                } finally {
                                                    // 执行后的善后工作，将beanName移出缓存。
                                                    afterSingletonCreation(beanName);
                                                }
                                    }
                                    // 如果singletonObjects缓存中存在该Bean则把该Bean直接放缓存中
                                    if (containsSingleton(beanName)) {
                                        	this.factoryBeanObjectCache.put(beanName, object);
                                    }
                                }
                        }
                        return object;
                    }
                } else {
            			// 获取FactoryBean中真实的Bean，并遍历执行后置钩子方法
                        Object object = doGetObjectFromFactoryBean(factory, beanName);
                        if (shouldPostProcess) {
                            try {
                                	object = postProcessObjectFromFactoryBean(object, beanName);
                            } catch (Throwable ex) {
                                	throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
                            }
                    }
                    return object;
            }
	}
```



##### #beforeSingletonCreation - 单例创建前

```java
// DefaultSingletonBeanRegistry
protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
}
```



##### #afterSingletonCreation - 单例创建后

```java
// DefaultSingletonBeanRegistry
protected void afterSingletonCreation(String beanName) {
    if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
        throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
    }
}
```

对比创建前后的方法调用可以发现，创建期间beanName是被保存在singletonsCurrentlyInCreation。

也就是说singletonsCurrentlyInCreation保存的是创建期间的beanName。



##### #postProcessObjectFromFactoryBean

```java
@Override
protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
    return applyBeanPostProcessorsAfterInitialization(object, beanName);
}
```

该方法就是对BeanPostProcess#postProcessAfterInitialization的遍历调用，具体细节先忽略。



## 总结

1. 转换BeanName，去除&前缀(FactoryBean的标志)，若是别名迭代取得原始名称。
2. 从缓存中获取Bean
3. 