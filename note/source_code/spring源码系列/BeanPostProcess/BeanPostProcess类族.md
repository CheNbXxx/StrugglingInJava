# BeanPostProcessor类族

> BeanPostProcessor是Spring中的核心钩子方法，它的许多子类分别提供了不同时间不同形式的回调。

<!-- more -->

---

[TOC]

## BeanPostProcessors

最最接口的钩子方法接口类。

提供了初始化前后的钩子方法。

 ![image-20200512111500702](/home/chen/github/_java/pic/image-20200512111500702.png)



## InstantiationAwareBeanPostProcessor

该接口在继承BeanPostProcessor的基础上扩展了在实例化前后的钩子方法。

 ![image-20200513065845525](/home/chen/github/_java/pic/image-20200513065845525.png)

调用postProcessBeforeInstantiation方法可以在Spring默认的实例化方法之前，定义自己的实例化方法。

例如生成代理并替换之类的，返回的Object若不为空则直接跳过实例化方法doCreateBean。

PostProcessAfterInstantiation方法则是在实例化之后但在属性填充之前的钩子方法，若返回false则跳过属性填充方法。

postProcessProperties也是在属性填充之前被调用，返回一个自定义的PropertyValues对象，若为空则跳过。



## SmartInstantiationAwareBeanPostProcessor

该接口扩展了InstantiationAwareBeanPostProcessor。

所以内部存在(3+3+2)8个钩子方法。

 ![image-20200513071519512](/home/chen/github/_java/pic/image-20200513071519512.png)

predictBeanType方法用于预测Bean的类型。

determineCandidateConstructors用于推断可用的构造函数。

getEarlyBeanReference用于获取早期引用，处理循环依赖的情况。





## MergedBeanDefinitionPostProcessor