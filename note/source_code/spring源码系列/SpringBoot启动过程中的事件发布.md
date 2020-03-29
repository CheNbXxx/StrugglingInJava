## SpringBoot启动过程中的事件发布机制



- Spring的事件机制是基于JDK提供了观察者模式的实现之一.



### 初始化

SpringBoot启动时,在初始化SpringApplication时就会通过工厂加载机制获取并保存所有的`ApplicationListener`实现.

另外的run方法中会对这些ApplicationListener进行包装.

```java
private SpringApplicationRunListeners getRunListeners(String[] args) {
    Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
    return new SpringApplicationRunListeners(logger,
                                             // 通过工厂加载机制获取所有的SpringApplicationRunListener子类实现,
                                             // 在spring.factory中标注的
                                             getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
}
```

SpringApplicationRunListeners是对SpringApplicationRunListener的简单封装,是最上层的发布类.

```java
class SpringApplicationRunListeners {
	private final Log log;
	// List...再简单不过的封装方式
	private final List<SpringApplicationRunListener> listeners;
  	// 发布方式就是通过for循环调用List的发布器发布.
    void starting() {
        for (SpringApplicationRunListener listener : this.listeners) {
            listener.starting();
        }
    }
}
```

对于SpringApplicationRunListener,其内部定义了SpringApplication启动过程中所有的事件发布的方法.

```java
// 或许可以理解为一种规范,在SpringBoot的启动过程中必然会有以下的事件.
// 我们如果想要实现自己的启动过程监听也要实现如下的方法.
public interface SpringApplicationRunListener {
	default void starting() {}

	default void environmentPrepared(ConfigurableEnvironment environment) {}

	default void contextPrepared(ConfigurableApplicationContext context) {}

	default void contextLoaded(ConfigurableApplicationContext context) {}

	default void started(ConfigurableApplicationContext context) {}

	default void running(ConfigurableApplicationContext context) {}

	default void failed(ConfigurableApplicationContext context, Throwable exception) {}
}
```

getApplicationListeners(event, type)而在SpringBoot中,它的默认实现只有EventPublishingRunListener,它通过对另外一种发布器的调用实现事件的发布.

EventPublishingRunListener的构造函数如下:

```java
public EventPublishingRunListener(SpringApplication application, String[] args) {
    this.application = application;
    this.args = args;
    // 定义一个事件发布器
    this.initialMulticaster = new SimpleApplicationEventMulticaster();
    // 可以看到这里已经把SpringApplication中的所有监听器填充到SpringApplicationRunListeners中了.
    for (ApplicationListener<?> listener : application.getListeners()) {
        this.initialMulticaster.addApplicationListener(listener);
    }
}
```

初始化的结果就是一个SpringApplicationRunListeners对象,持有所有的SpringApplicationRunListener子类的引用集合.

而默认的发布逻辑都是在EventPublishingRunListener中实现的.



### 默认的11个监听器

