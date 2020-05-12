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

1. 解析出Class对象
2. 检查重写方法
3. 执行BeanPostProcess相关代码
4. 正式创建

以上是按照当前方法来说，完整逻辑在文末。





### #resolveBeanClass - 解析Class对象

```java
@Nullable
protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
      throws CannotLoadBeanClassException {
   try {
      if (mbd.hasBeanClass()) {
         return mbd.getBeanClass();
      }
      if (System.getSecurityManager() != null) {
         return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () ->
            doResolveBeanClass(mbd, typesToMatch), getAccessControlContext());
      }
      else {
         return doResolveBeanClass(mbd, typesToMatch);
      }
   }
   catch (PrivilegedActionException pae) {
      ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
      throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
   }
   catch (ClassNotFoundException ex) {
      throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
   }
   catch (LinkageError err) {
      throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
   }
}
```





### #resolveBeforeInstantiation - 实例化前的工作

该方法也可以负责创建Bean实例，如果创建了自定义Bean实例就不需要在通过Spring的doCreateBean创建。

```java
// AbstractAutowireCapableBeanFactory
@Nullable
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
    	// 根据mbd判断该Bean对象是否需要执行初始化前的工作
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



判断是否有InstantiationAwareBeanPostProcessor的方法：

```java
protected boolean hasInstantiationAwareBeanPostProcessors() {
		return this.hasInstantiationAwareBeanPostProcessors;
}
```

该方法直接返回的成员变量hasInstantiationAwareBeanPostProcessors。

查看了该成员变量的调用处之后发现，在addBeanPostProcessors中会将其置位true，代码如下：

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

可以看到，整体的判断逻辑也很简单，只要往BeanFactory中添加的BeanPostProcessor是InstantiationAwareBeanPostProcessor，那么此时就会执行该逻辑。



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

[]()



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
            	instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            	instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        final Object bean = instanceWrapper.getWrappedInstance();
        Class<?> beanType = instanceWrapper.getWrappedClass();
        if (beanType != NullBean.class) {
            	mbd.resolvedTargetType = beanType;
        }

        // Allow post-processors to modify the merged bean definition.
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
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                                          isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
                if (logger.isTraceEnabled()) {
                        logger.trace("Eagerly caching bean '" + beanName +
                                     "' to allow for resolving potential circular references");
                }
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

实例的创建方法，会有不同的实例化策略。

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

