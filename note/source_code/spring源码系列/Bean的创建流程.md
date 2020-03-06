### Spring Bean的生命周期，创建流程



Spring IOC只会帮助管理单例模式的Bean的生命周期，而不会管理原型模式等。



1. 获取BeanDefinition，也是实例化前置处理，InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation()方法,该方法可以在实例化前修改Bean的定义，也就是BeanDefinition。
2. 实例化，BeanDefinition转化为BeanWarpper之后，调用方法创建实例
3. 属性填充，popularBean()，完成依赖的注入，包括Aware类的注入。
4. 属性填充后置处理：调用容器中InstantiationAwareBeanPostProcessor的postProcessAfterInstantiation()方法，进行属性填充后处理。
5. 初始化前置处理调用BeanPostProcessor的postProcessBeforeInitialization()
6. 初始化
7. 初始化后置处理，调用BeanPostProcessor的postProcessAfterInitialization()执行初始化后置处理。