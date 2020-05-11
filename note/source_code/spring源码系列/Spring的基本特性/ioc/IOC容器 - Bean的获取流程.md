# IOC容器 - Bean的获取流程

> 本文主要讲的是AbstractBeanFactory类中doGetBean方法的流程。
>
> 相关的获取流程只会关注于单例模式的。

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
            	// 如果直接从缓存中获取到了，为什么还要经过getObjectForBeanInstance方法
            	// 是处理从下两级缓存获取的对象吗
                bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}else {
                // 以下的流程就是缓存中没有获取到对象，或者arg参数不为空的情况

                // Fail if we're already creating this bean instance:
                // We're assumably within a circular reference.
                // 原型模式直接抛出异常
                if (isPrototypeCurrentlyInCreation(beanName)) {
                        throw new BeanCurrentlyInCreationException(beanName);
                }
            
            	// 下面这一段代码在没有从缓存中获取到对象，或者arg参数不为空，且不是在原型模式的创建中

                // Check if bean definition exists in this factory.
                // 判断BeanDefinition是否在父级的BeanFactory中注册的
                BeanFactory parentBeanFactory = getParentBeanFactory();
                // 如果父级的BeanFactory不为空，并且beanName不存在于当前BeanFactory
                if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                        // Not found -> check parent.
                        // 规范化BeanName，在去&并且去别名之后，
                        // 如果原来的BeanName带&，则之后也要加上
                        String nameToLookup = originalBeanName(name);
                        // 各种通道获取Bean对象
                        if (parentBeanFactory instanceof AbstractBeanFactory) {
                                return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
                                        nameToLookup, requiredType, args, typeCheckOnly);
                        } else if (args != null) {
                                // Delegation to parent with explicit args.
                                return (T) parentBeanFactory.getBean(nameToLookup, args);
                        } else if (requiredType != null) {
                                // No args -> delegate to standard getBean method.
                                return parentBeanFactory.getBean(nameToLookup, requiredType);
                        } else {
                                return (T) parentBeanFactory.getBean(nameToLookup);
                        }
			}

           // 标记Bean对象正在创建
			if (!typeCheckOnly) {
				markBeanAsCreated(beanName);
			}

			try {
                // 获取合成后的BeanDefinition
                // 因为上面标记对象正在创建时就把之前合成的RootBeanDefinition设置为旧的
                // 所以此处都会重新获取RootBeanDefinition
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// Guarantee initialization of beans that the current bean depends on.
                // 解决依赖问题
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
                                } catch (NoSuchBeanDefinitionException ex) {
                                    throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                            "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
                                }
                        }
				}

				// Create bean instance.
                // 分各种模型的创建
				if (mbd.isSingleton()) {
                    // 单例模型的创建
					sharedInstance = getSingleton(beanName, () -> {
						try {
								return createBean(beanName, mbd, args);
						} catch (BeansException ex) {
                                // Explicitly remove instance from singleton cache: It might have been put there
                                // eagerly by the creation process, to allow for circular reference resolution.
                                // Also remove any beans that received a temporary reference to the bean.
                                destroySingleton(beanName);
                                throw ex;
						}
					});
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}	else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
                            beforePrototypeCreation(beanName);
                            // 原型模式这里就是直接创建的
                            prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				} else {
                    	// 其他的生命周期
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
                                }  finally {
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
			}	catch (BeansException ex) {
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



### #getSingleton - 三级缓存中获取Bean

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



#### #isSingletonCurrentlyInCreation - 判断对象是否正在创建

```java
public boolean isSingletonCurrentlyInCreation(String beanName) {
    	// singletonsCurrentlyInCreation应该就是保存正在创建中的Bean的名称的
		return this.singletonsCurrentlyInCreation.contains(beanName);
}
```

该方法主要用于判断beanName对应的Bean对象是否在创建中。

判断的依据也很简单，就是beanName在不在singletonsCurrentlyInCreation集合中。



### #markBeanAsCreated - 标记Bean对象正在创建

```java
protected void markBeanAsCreated(String beanName) {
    	// 双重检查beanName，最后添加到alreadyCreated的缓存中
        if (!this.alreadyCreated.contains(beanName)) {
                synchronized (this.mergedBeanDefinitions) {
                        if (!this.alreadyCreated.contains(beanName)) {
                                // Let the bean definition get re-merged now that we're actually creating
                                // the bean... just in case some of its metadata changed in the meantime.
                                clearMergedBeanDefinition(beanName);
                                this.alreadyCreated.add(beanName);
                        }
                }
        }
}

protected void clearMergedBeanDefinition(String beanName) {
    	// 将合成的BeanDefinition的缓存设置为无效
        RootBeanDefinition bd = this.mergedBeanDefinitions.get(beanName);
        if (bd != null) {
            	bd.stale = true;
        }
}
```

标记Bean对象正在创建的做法就是将beanName添加到alreadyCreated中。

此处将合成的BeanDefinition清除之后，如上文中的方法`getMergedBeanDefinition`以及`getMergedLocalBeanDefinition`中，都会重新去合成RootBeanDefinition。



### #getMergedLocalBeanDefinition - 获取合成的BeanDefinition

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

该方法的作用就是获取一个合并的BeanDefinition，这里的合并是指和父BeanDefinition的合并，就是拿子BeanDefinition去覆盖父BeanDefinition的一些属性。

获取流程也不复杂：

1. 从缓存中获取已经合并了的RootBeanDefinition，获取到了并且不为旧的就直接返回
2. 从beanDefinitionMap中获取BeanDefinition，这步是必须要获取到的，没有则直接报错。
3. 将获取到的BeanDefinition与其父BeanDefinition合并，如果有的话。



#### #getBeanDefinition - 从存量中获取BeanDefinition

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



#### #getMergedBeanDefinition - 获取合并的BeanDefinition

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



### #getSingleton - 获取单例模式的Bean对象

该方法和上面的那个方法不同，注意区分，并且该方法属于DefaultSingletonBeanRegistry类。

```java
// DefaultSingletonBeanRegistry
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "Bean name must not be null");
		synchronized (this.singletonObjects) {
            // 从缓存中获取对象
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
                    if (this.singletonsCurrentlyInDestruction) {
                        throw new BeanCreationNotAllowedException(beanName,
                                "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                                "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                    }
                	// 单例Bean对象创建前的逻辑，之前有讲过
                    beforeSingletonCreation(beanName);
                    boolean newSingleton = false;
                    boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                    if (recordSuppressedExceptions) {
                        	this.suppressedExceptions = new LinkedHashSet<>();
                    }
                    try {
                            // 这里不是FactoryBean的getObjects
                        	// 而是调用方传入的方法，从doGetBean进来时就是调用的createBean方法，
                            // 创建流程可以在另外一个文件中看到
                            singletonObject = singletonFactory.getObject();
                            newSingleton = true;
                    } catch (IllegalStateException ex) {
                            // Has the singleton object implicitly appeared in the meantime ->
                            // if yes, proceed with it since the exception indicates that state.
                            singletonObject = this.singletonObjects.get(beanName);
                            if (singletonObject == null) {
                                    throw ex;
                            }
				} catch (BeanCreationException ex) {
					if (recordSuppressedExceptions) {
                            for (Exception suppressedException : this.suppressedExceptions) {
                                	ex.addRelatedCause(suppressedException);
                            }
					}
					throw ex;
				} finally {
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
                    // 这里是创建之后的处理，逻辑与之前相同
					afterSingletonCreation(beanName);
				}
				if (newSingleton) {
                    // 如果是个新的实例对象，则需要添加到缓存中。
					addSingleton(beanName, singletonObject);
				}
			}
			return singletonObject;
		}
	}
```

该方法就是获取Bean对象的方法，并且是通过传入的ObjectFactory获取。

方法中有很多的异常处理逻辑暂时忽略。

整体的逻辑就很简单了：

1. 检查缓存中是否存在对象，存在就直接返回了
2. 不存在则是和上文基本一致的创建逻辑，标记，创建对象，去除标记

这里可以看一下创建Bean之后是如何添加缓存的，通过addSingleton方法

```java
protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
                this.singletonObjects.put(beanName, singletonObject);
                this.singletonFactories.remove(beanName);
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
        }
}
```

涉及到的集合就如上所示了。

singletonObjects是缓存创建好的Bean对象的。

earlySingletonObjects是存放的早期引用。

另外就是对BeanPostProcess的处理并没有包含在这个方法里面，也就是说是包含在传入的ObjectFactory里面。





### #getObjectForBeanInstance - 从入参的beanInstance中获取对象

```java
protected Object getObjectForBeanInstance(
      Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
   // Don't let calling code try to dereference the factory if the bean isn't a factory.
    // 根据name是否是需要FactoryBean对象，是的话就直接返回。
    // 但如果beanInstance不是FactoryBean也会报错。
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
       	 // 配置RootBeanDefinition的FactoryBean属性
       	 // 这个顺手设置的意义在哪，暂时未知
          if (mbd != null) {
             	mbd.isFactoryBean = true;
          }
       	  // 直接返回对象
          return beanInstance;
   }

   // Now we have the bean instance, which may be a normal bean or a FactoryBean.
   // If it's a FactoryBean, we use it to create a bean instance, unless the
   // caller actually wants a reference to the factory.
   // 如果beanInstance是普通Bean对象则直接返回。
   if (!(beanInstance instanceof FactoryBean)) {
      		return beanInstance;
   }
    
    // 剩下的就是没有&前缀的FactoryBean类型了，
    // 获取bean时没有带&前缀表示希望获取getObject的对象。

   Object object = null;
   if (mbd != null) {
              mbd.isFactoryBean = true;
   } else {
            // 从factoryBeanObjectCache缓存中获取对象
            object = getCachedObjectForFactoryBean(beanName);
   }
 	// 缓存中不存在则开始创建逻辑
   if (object == null) {
          // Return bean instance from factory.
          FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
          // Caches object obtained from FactoryBean if it is a singleton.
          // 获取合并BeanDefinition
          if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
          }
          boolean synthetic = (mbd != null && mbd.isSynthetic());
       		// 具体的getObject调用
      	  // !synthetic就是shouldPostProcess
          object = getObjectFromFactoryBean(factory, beanName, !synthetic就是showshouldPostProcess);
   }
   return object;
}
```

该类在doGetBean中调用的地方好像有点多，多到我有点模糊它的具体作用。

 ![image-20200510004015662](/home/chen/github/_java/pic/image-20200510004015662.png)

以上是方法的注释：获取指定的Bean实例对象，可能是它本身，如果是FactoryBean的话就创建对象。

联想到FactoryBean的获取方式我才清楚了这个类的获取逻辑：

入参中的name是doGetBean中的用来获取Bean的入参，beanName是去别名去前缀之后的名称。

beanInstance是根据beanName从缓存中获取的对象，它有可能是实际的bean对象，也有可能是是FactoryBean对象。

因此就有以下几种情况：

1. 如果name带有&前缀则返回当前的FactoryBean对象，如果name带有前缀，但beanInstance不是FactoryBean则报错。
2. 如果beanInstance是普通的Bean对象则直接返回。
3. 如果beanInstance是FactoryBean，但获取的name没有带&前缀，则表示需要获取FactoryBean#getObject的对象。

逻辑中对FactoryBean#getObject的结果也是有缓存的，就在`factoryBeanObjectCache`Map中，key为beanName，value为getObject返回的对象。

另外就是调用FactoryBean#getObject方法时是否需要执行BeanPostProcessor的判断，先是获取了BeanDefinition。

获取的BeanDefinition可能是合成的也可能不是合成的(没有父级的BeanDefinition)。

合成的BeanDefinition在调用BeanFactory时是不需要遍历执行BeanPostProcessor的。





#### #getCachedObjectForFactoryBean - 缓存中获取FactoryBean对象

```java
// 继承自FactoryBeanRegistrySupport的方法
@Nullable
protected Object getCachedObjectForFactoryBean(String beanName) {
    	return this.factoryBeanObjectCache.get(beanName);
}
```

factoryBeanObjectCache就是存放beanName和对应getObject结果的映射的Map。

beanName是去前缀去别名之后的名称。



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

该方法的主要功能就是从FactoryBean中获取Bean对象。

整体逻辑主要分两块，单例类和非单例类，单例类需要缓存，而非单例类每次都生成新的对象。

单例部分逻辑如下：

1. 先判断缓存中是否有对象，有就直接返回
2. 没有则进入创建流程，先调用FactoryBean的getObject方法获取Bean对象
3. 获取以后会有第二次检查缓存是否存在获取的对象，有就返回该对象
4. 没有就进入创建的下一个流程，先标记创建流程，执行BeanPostProcessor后在撤销创建流程
5. 最后如果是新的Bean对象则添加缓存。

**整个逻辑让我意外的是，单例对象在singletonsCurrentlyInCreation中的时间，也就是创建期间，指的是BeanPostProcessor的遍历过程。**

另外在创建完成之后会以原始名称和FactoryBean返回值的映射关系添加到缓存factoryBeanObjectCache中。

非单例模式的创建更简单，创建后直接遍历BeanPostProcessor就好。



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



##### #postProcessObjectFromFactoryBean - BeanPostProcessor

```java
@Override
protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
    return applyBeanPostProcessorsAfterInitialization(object, beanName);
}
```

该方法就是对BeanPostProcess#postProcessAfterInitialization的遍历调用，具体细节先忽略。



## doGetBean的整体流程

1. 处理name，去除&前缀，去别名。
2. 三级缓存获取Bean对象，三级缓存分别是
   1. singletonObjects
   2. earlySingletonObjects
   3. singletonFactory
3. 缓存中没有则判断父级的BeanFactory是否有该Bean的实例化对象，有就返回
4. 没有则开始创建流程，先获取合成的RootBeanDefinition
5. 递归处理依赖问题
6. 区分不同的生命周期实例化对象并返回

哈哈哈，怎么整理下来好像流程不多的样子。

getObjectForBeanInstance方法更多的是对获取到的实例化对象的处理：

**如果getBean时使用的带&前缀的名称表示希望获取BeanFactory对象，而非getObject的结果。**

接下来就是依赖处理和CreateBean的流程了。

详细的可以看：

