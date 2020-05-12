# IOC容器 - Bean的创建流程

> 从doGetBean的方法切入，解析Bean的创建流程。
>
> 重点要应该关注对BeanPostProcesser的调用流程，这是Spring中最重要的关键点了，甚至没有之一。

<!-- more -->

---

[TOC]

## 概述

Spring最最核心的特性就是IOC和AOP，而创建过程是IOC和AOP的连接点，所以说该方法最最重要。

本人看这个方法是在doGetBean中切入的。





## AbstractAutowireCapableBeanFactory#createBean

以下代码即为源码，不过省略了异常处理的方法，捕获之后再次抛出的，感觉省略代码会更清晰。

```java
// AbstractAutowireCapableBeanFactory
@Override
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
      throws BeanCreationException {
	...
   RootBeanDefinition mbdToUse = mbd;
   // Make sure bean class is actually resolved at this point, and
   // clone the bean definition in case of a dynamically resolved Class
   // which cannot be stored in the shared merged bean definition.
    // 从mbd中解析出Bean对应的Class对象
   Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
   if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
          mbdToUse = new RootBeanDefinition(mbd);
          mbdToUse.setBeanClass(resolvedClass);
   }
   // Prepare method overrides.
  ...
            // 检查覆写/重写的方法
           mbdToUse.prepareMethodOverrides();
   ...
          // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
           // 嗯。。给BeanPostProcesser一个机会
       		// 看着好像是通过postProcessBeforeInstantiation方法来实例化Bean
       		// 对象实例化之后也会执行BeanPostProcessor  After钩子方法
          Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
          if (bean != null) {
             	return bean;
          }
	...
       	 // doXXXX的一般都是核心方法，比如doGetBean
        // 这里应该是核心的Bean创建方法
          Object beanInstance = doCreateBean(beanName, mbdToUse, args);
    ...
     	 return beanInstance;
	 ...
}
```

整体的代码逻辑很清晰

1. 解析出Class对象 - #resolveBeanClass
2. 检查重写方法 - #prepareMethodOverrides
3. 执行BeanPostProcess相关代码 - resolveBeforeInstantiation，此时如果Bean已经创建，说明用户已经自定义了创建过程，直接返回。
4. 正式创建 - doCreateBean

以上是基于当前方法来说，完整逻辑在文末。



接下来先来看解析Class对象的过程。

### #resolveBeanClass - 解析Class对象

```java
@Nullable
protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
      throws CannotLoadBeanClassException {
   ./..
       	   // 一般来说此步就退出了
          if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
          }
    	  // 和Java安全管理器有关的，暂时没看懂，先忽略
          if (System.getSecurityManager() != null) {
                 return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () ->
                    doResolveBeanClass(mbd, typesToMatch), getAccessControlContext());
          } else {
                return doResolveBeanClass(mbd, typesToMatch);
          }
	...
}
```

方法中先从mbd中获取，如果有的话就直接返回了。

之后是和Java安全管理器相关，有的话走这条路线应该比较安全。

最后到了doResolveBeanClass方法，do开头的方法一般都比较核心。

doResolveBeanClass的逻辑暂时忽略。



### #resolveBeforeInstantiation - 实例化前的工作

说是实例化之前，但是**该方法也可以直接实例化Bean**，那之后就没有Spring自己的实例化逻辑什么事了。

```java
// AbstractAutowireCapableBeanFactory
@Nullable
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
    	// 根据mbd判断该Bean对象是否执行过初始化前的工作
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
                // Make sure bean class is actually resolved at this point.
            	// 确定此时已经解析了该类，对这个注释的意思不明
            	// 判断条件：
            	// 1. mbd不是合成的
            	// 2. 当前BeanFactory有InstantiationAwareBeanPostProcessor
                if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
                    // 获取目标类型
                    Class<?> targetType = determineTargetType(beanName, mbd);
                    if (targetType != null) {
                        	// 遍历调用前置方法
                            bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                        	// 如果不为空，继续执行后置的钩子方法
                            if (bean != null) {
                                	bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                            }
                    }
            }
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
}
```



判断是否有InstantiationAwareBeanPostProcessor的方法如下：

```java
protected boolean hasInstantiationAwareBeanPostProcessors() {
		return this.hasInstantiationAwareBeanPostProcessors;
}
```

该方法直接返回的成员变量hasInstantiationAwareBeanPostProcessors。

查看了该成员变量的调用处之后发现，在addBeanPostProcessors方法中会将其置为true，代码如下：

```java
// AbstractBeanFactory
public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
    ...
        // Track whether it is instantiation/destruction aware
        if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
            	this.hasInstantiationAwareBeanPostProcessors = true;
        }
    ...
}
```

因此整体的判断逻辑也很简单，只要往BeanFactory中添加的BeanPostProcessor是InstantiationAwareBeanPostProcessor，那么此时就会执行该逻辑。

#### #applyBeanPostProcessorsBeforeInstantiation - 实例化前置处理

```java
// AbstractAutowireCapableBeanFactory
@Nullable
protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
    for (BeanPostProcessor bp : getBeanPostProcessors()) {
        if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
            // 如果获取到的bean不为空，则直接返回。
            if (result != null) {
                	return result;
            }
        }
    }
    return null;
}
```

整体逻辑就是遍历全类的BeanPostProcess找出InstantiationAwareBeanPostProcessor的实现类，然后调用执行。

**此时要注意的是如果获取到结果的Bean对象就直接返回了，所以如果有一个以上子类要执行时，就需要考虑顺序和返回了。**

在SpringBoot Servlet Web环境中，debug的结果只有一个实现类就是`ImportAwareBeanPostProcessor`。

具体的`ImportAwareBeanPostProcessor`的作用可以看下面：



**实例化之后还是会执行BeanPostProcessor的后置钩子方法。**

#### #applyBeanPostProcessorsAfterInitialization - 实例化后置处理

```java
// AbstractAutowireCapableBeanFactory
@Override
public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
    throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
                Object current = processor.postProcessAfterInitialization(result, beanName);
                if (current == null) {
                    	return result;
                }
                result = current;
        }
        return result;
}
```



## #doCreateBean - 创建Bean对象

```java
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
    throws BeanCreationException {
        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            	// 从factoryBeanInstanceCache中删除对象
            	instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            	// 先创建对象实例
            	// Spring自定义的创建逻辑有六个之多，会从指选择一个进行调用。
            	// 最后返回的是BeanWrapper
            	instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
    	// 被包装的对象和类型
        final Object bean = instanceWrapper.getWrappedInstance();
        Class<?> beanType = instanceWrapper.getWrappedClass();
        if (beanType != NullBean.class) {
            	mbd.resolvedTargetType = beanType;
        }

        // Allow post-processors to modify the merged bean definition.
    	// 调用MergedBeanDefinitionPostProcessor方法
        synchronized (mbd.postProcessingLock) {
                if (!mbd.postProcessed) {
                    try {
                        	applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                    } catch (Throwable ex) {
                        	throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                                        "Post-processing of merged bean definition failed", ex);
                    }
                    mbd.postProcessed = true;
                }
        }

        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
    	// 是否需要暴露早期引用
    	// 条件有三：1. 单例模式 2.允许循环引用 3.单例正在创建
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                                          isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
                if (logger.isTraceEnabled()) {
                        logger.trace("Eagerly caching bean '" + beanName +
                                     "' to allow for resolving potential circular references");
                }
            	// 这里就是暴露早期引用的逻辑吧
                addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
        }

        // Initialize the bean instance.
        Object exposedObject = bean;
        try {
                populateBean(beanName, mbd, instanceWrapper);
                exposedObject = initializeBean(beanName, exposedObject, mbd);
        }  catch (Throwable ex) {
                if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                        throw (BeanCreationException) ex;
                }   else {
                        throw new BeanCreationException(
                            mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
                }
        }

        if (earlySingletonExposure) {
                Object earlySingletonReference = getSingleton(beanName, false);
                if (earlySingletonReference != null) {
                    if (exposedObject == bean) {
                            exposedObject = earlySingletonReference;
                    } else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                            String[] dependentBeans = getDependentBeans(beanName);
                            Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                            for (String dependentBean : dependentBeans) {
                                    if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                                            actualDependentBeans.add(dependentBean);
                                    }
                            }
                            if (!actualDependentBeans.isEmpty()) {
                                throw new BeanCurrentlyInCreationException(beanName,
                                                                           "Bean with name '" + beanName + "' has been injected into other beans [" +
                                                                           StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                                                                           "] in its raw version as part of a circular reference, but has eventually been " +
                                                                           "wrapped. This means that said other beans do not use the final version of the " +
                                                                           "bean. This is often the result of over-eager type matching - consider using " +
                                                                           "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
                            }
                        }
                }
        }

        // Register bean as disposable.
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(
                mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
}

```





### #createBeanInstance - 创建Bean的实例对象

实例的创建方法，会有不同的实例化策略，该方法不涉及BeanPostProcessor的调用。

```java
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
   // Make sure bean class is actually resolved at this point.
    // 该方法上面解析过
   Class<?> beanClass = resolveBeanClass(mbd, beanName);
	
    // 看报错信息就知道了
    // 类不是public的，无法访问。
   if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
          throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
   }
	
    // 这里是第一种创建方法
    // 1. 通过mbd存在的instanceSupplier
    // Supplier是Java8提供了函数式接口，通过无参get方法获取对象
   Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
   if (instanceSupplier != null) {
      		return obtainFromSupplier(instanceSupplier, beanName);
   }
	
    // 这里是第二种方法
    //  2.通过工厂方法获取
   if (mbd.getFactoryMethodName() != null) {
      		return instantiateUsingFactoryMethod(beanName, mbd, args);
   }

   // Shortcut when re-creating the same bean...
    // 3. 重新创建相同的Bean实例的方法
   boolean resolved = false;
   boolean autowireNecessary = false;
   if (args == null) {
          synchronized (mbd.constructorArgumentLock) {
                 if (mbd.resolvedConstructorOrFactoryMethod != null) {
                        resolved = true;
                        autowireNecessary = mbd.constructorArgumentsResolved;
                 }
          }
   }
   if (resolved) {
          if (autowireNecessary){ 
              	return autowireConstructor(beanName, mbd, null, null);
          } else {
                return instantiateBean(beanName, mbd);
          }
   }

   // Candidate constructors for autowiring?
   // 4.通过Autowire创建
   // 具体的创建方式未知
   Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
   if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
         mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
      		return autowireConstructor(beanName, mbd, ctors, args);
   }

   // Preferred constructors for default construction?
    // 5.使用首选的构造函数创建
   ctors = mbd.getPreferredConstructors();
   if (ctors != null) {
      	return autowireConstructor(beanName, mbd, ctors, null);
   }

   // No special handling: simply use no-arg constructor.
    // 6. 使用默认的构造函数创建
   return instantiateBean(beanName, mbd);
}
```

以上可以看出Bean对象的实例化有六种方式

1. 直接通过Supplier接口获取对象
2. 通过BeanDefinition中配置的工厂方法
3. 创建的过的实例对象走相同的方法。
4. autowireConstructor
5. 首选构造函数
6. 默认无参构造

暂时都不深入。



### #applyMergedBeanDefinitionPostProcessors

```java
protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof MergedBeanDefinitionPostProcessor) {
                        MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
                        bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
                }
        }
}
```

和之前一样，就是遍历BeanPostProcessor筛选出MergedBeanDefinitionPostProcessor然后执行。

Debug发现最终执行的只有ApplicationListenerDetector。

暂时忽略，以后分析。



### #addSingletonFactory - 暴露早期引用

是否暴露早起引用的判断条件如下：

```java
boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                                  isSingletonCurrentlyInCreation(beanName));
```

单行的逻辑判断语句，需要暴露早期引用需要满足以下情况：

1. 单例模式
2. 允许循环引用
3. beanName正在创建

之后就是暴露早期引用的具体逻辑：

```java
// 该行代码是doCreateBean中的一行
// 因为传递的有内部类，所以放这里
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean))；
   
protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
                if (!this.singletonObjects.containsKey(beanName)) {
                        this.singletonFactories.put(beanName, singletonFactory);
                        this.earlySingletonObjects.remove(beanName);
                        this.registeredSingletons.add(beanName);
                }
        }
}
```

整个addSingletonFactory方法就是对缓存的操作序列，使用singletonObjects作为锁。

暂时不清楚此时添加缓存的意义。

下文是getEarlyBeanReference的源码：

```java
// 获取早期引用
protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
                for (BeanPostProcessor bp : getBeanPostProcessors()) {
                        if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                                SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                                exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
                        }
                }
        }
        return exposedObject;
}
```

看代码好像就是对SmartInstantiationAwareBeanPostProcessor的调用。



暴露完早期引用接下来就是属性填充了。

### #populateBean - 属性填充

```java
@SuppressWarnings("deprecation")  // for postProcessPropertyValues
protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
    // BeanWrapper为空时就退出了
    if (bw == null) {
            if (mbd.hasPropertyValues()) {
                    throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            }
            else {
                    // Skip property population phase for null instance.
                    return;
            }
    }

    // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
    // state of the bean before properties are set. This can be used, for example,
    // to support styles of field injection.
    // 执行InstantiationAwareBeanPostProcessors的后置钩子方法
    if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
                            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                            if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                                    return;
                            }
                    }
            }
    }
	// 获取PropertyValues属性
    PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);
	
    // 获取自动注入的模式
    int resolvedAutowireMode = mbd.getResolvedAutowireMode();
    // 按照类型或者名称注入
    // autowireByName和Type先忽略，这是注入属性的方法
    if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
        MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
        // Add property values based on autowire by name if applicable.
        if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
            	autowireByName(beanName, mbd, bw, newPvs);
        }
        // Add property values based on autowire by type if applicable.
        if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
            	autowireByType(beanName, mbd, bw, newPvs);
        }
        pvs = newPvs;
    }

    boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
    boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);

    PropertyDescriptor[] filteredPds = null;
    if (hasInstAwareBpps) {
            if (pvs == null) {
                	pvs = mbd.getPropertyValues();
            }
        	// 这里遍历BeanPostProcessor中的InstantiationAwareBeanPostProcessor
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
                        	// 调用InstantiationAwareBeanPostProcessor#postProcessProperties方法
                        	// 该方法可以用来替换mbd中的PropertyValues
                            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                            PropertyValues pvsToUse = ibp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
                            if (pvsToUse == null) {
                                    if (filteredPds == null) {
                                            filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
                                    }
                                	// postProcessPropertyValues方法已经被标记过时，不知道为什么这里还在调用。
                                    pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
                                    if (pvsToUse == null) {
                                            return;
                                    }
                            }
                            pvs = pvsToUse;
                    }
            }
    }
    if (needsDepCheck) {
            if (filteredPds == null) {
                filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
            }
            checkDependencies(beanName, mbd, filteredPds, pvs);
    }

    if (pvs != null) {
        applyPropertyValues(beanName, mbd, bw, pvs);
    }
}

```



