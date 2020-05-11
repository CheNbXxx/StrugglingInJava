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
          Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
          if (bean != null) {
             	return bean;
          }
	...
       	 // doXXXX的一般都是核心方法，比如doGetBean
          Object beanInstance = doCreateBean(beanName, mbdToUse, args);
    ...
     	 return beanInstance;
	 ...
}
```

整体的代码逻辑很清晰

1. 解析Class对象
2. 检查方法的重写
3. 执行BeanPostProcess相关代码
4. 正式创建

以上是按照当前方法来说，完整逻辑在文末。





### #resolveBeforeInstantiation - 实例化前的工作

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



