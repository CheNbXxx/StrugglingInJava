# BeanFactoryPostProcessor

- BeanFactoryPostProcessor是SpringBoot中的钩子方法，实现该接口可以自由修改BeanDefinition以及BeanFactory的信息。

- BeanFactoryPostProcessor在上下文刷新时被调用。

  ![image-20200415141624287](/home/chen/github/_java/pic/image-20200415141624287.png) 





## BeanFactoryPostProcessor接口

 ![image-20200415142559489](/home/chen/github/_java/pic/image-20200415142559489.png)

BeanFactoryPostProcessor只提供了postProcessorBeanFactory方法。

