# SpringBoot启动过程中的上下文准备



```java
prepareContext(context, environment, listeners, applicationArguments, printedBanner);
```

`prepareContext`方法是`refresh`之前最后调用的方法，是对之前创建的Context对象的填充和准备。



```java
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
    	// 设置环境变量,Context中还包含了BeanDefinitionReader和BeanDefinitionScanner都会设置
		context.setEnvironment(environment);
    	// 对Context的简单处理，会注册Bean名称生成器，ResourceLoader和ConversionService
		postProcessApplicationContext(context);
    	// 调用所有的ApplicationContextInitializer
    	// 此处的initializers就是在SpringApplication构造函数中通过工厂加载机制获取的
		applyInitializers(context);
    	// 发布ApplicationContextInitializedEvent
		listeners.contextPrepared(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
    	// 将命令行参数注册为Bean对象
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
    	// Banner也注册未对象...
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
    	// 设置是否允许BeanDefinition重复
    	// spring.main.allow-bean-definition-overridiing配置项相关
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
    	// 增加一个BeanFactoryPostProcessor
		if (this.lazyInitialization) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		// 获取所有的属性包括primarySources和sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
    	// 创建一个BeanDefinitionLoader，并按照sources类型分别做加载操作
		load(context, sources.toArray(new Object[0]));
    	// 发布ApplicationPreparedEvent
		listeners.contextLoaded(context);
	}
```



## 环境设置

- 将环境应用到当前的应用上下文
- 同时会设置上下文中`AnnotatedBeanDefinitionReader`和`ClassPathBeanDefinitionScanner`两个成员变量的相关属性。

```java
// AnnotationConfigServletWebServerApplicationContext
@Override
public void setEnvironment(ConfigurableEnvironment environment) {
    // 设置当前ApplicationContext的环境变量
    super.setEnvironment(environment);
    this.reader.setEnvironment(environment);
    this.scanner.setEnvironment(environment);
}

// AnnotatedBeanDefinitionReader
public void setEnvironment(Environment environment) {
    // ConditionEvaluator是Spring条件注解的实现基础，用于处理@ConditionalOnClass等类似注解
    this.conditionEvaluator = new ConditionEvaluator(this.registry, environment, null);
}

// ClassPathScanningCandidateComponentProvider
// ClassPathScanningCandidateComponentProvider是ClassPathBeanDefinitionScanner的父类
public void setEnvironment(Environment environment) {
    Assert.notNull(environment, "Environment must not be null");
    this.environment = environment;
    this.conditionEvaluator = null;
}

```



## postProcessApplicationContext

- 为beanNameGenerator注册Bean实例
- 设置ApplicationContext的资源加载器
- 设置上下文中BeanFactory的属性转化工具

```java
protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
    // 如果BeanNameGenerator已经生成就将其注入到上下文中
    if (this.beanNameGenerator != null) { 		
        context.getBeanFactory()
        	.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
                                                   this.beanNameGenerator);
    }
    // 如果资源加载器不为空也添加到上下文中
    if (this.resourceLoader != null) {
        // 区分不同的上下文类型有不同的添加方式
        if (context instanceof GenericApplicationContext) {
            ((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
        }
        if (context instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) context).setClassLoader(this.resourceLoader.getClassLoader());
        }
    }
    // 配置BeanFactory的属性转化工具类
    if (this.addConversionService) {
        context.getBeanFactory()
            .setConversionService(ApplicationConversionService.getSharedInstance());
 }
```



## applyInitializers

- 调用全部的`ApplicationContextInitializer`
- 所有的`ApplicationContextInitializer`都是在`SpringApplication`的构造函数中通过工厂加载机制获取的。

```java
// SpringApplication
protected void applyInitializers(ConfigurableApplicationContext context) {
    for (ApplicationContextInitializer initializer : getInitializers()) {
        // 两行的断言相关，可以暂时忽略
        Class<?> requiredType = GenericTypeResolver
            .resolveTypeArgument(initializer.getClass(),ApplicationContextInitializer.class);
        Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
        // 调用initialize方法，讲创建的上下文作为入参
        initializer.initialize(context);
    }
}

// SpringApplication
public Set<ApplicationContextInitializer<?>> getInitializers() {
    // 讲SpringApplication中的初始化类集合包装一下返回
    return asUnmodifiableOrderedSet(this.initializers);
}
```

以下是Debug发现的7个`ApplicationContextInitializer`实现类

 ![image-20200117110430572](C:\Users\TT\AppData\Roaming\Typora\typora-user-images\image-20200117110430572.png)

举例说明:

1. `DelegatingApplicationContextInitializer`

   获取环境属性中`context.initializer.classes`的`ApplicationContextInitializer`子类，并遍历执行。

2. `ContextIdApplicationContextInitializer`

   设置`contextId`，并向BeanFactory中注册一个`ContetxtId`的Bean对象。

3. `SharedMetadataReaderFactoryContextInitializer`

   添加一个`CachingMetadataReaderFactoryPostProcessor`的`BeanFactoryPostProcessor`。

   

## 发布ApplicationContextInitializedEvent事件

- 常规的调用套路，最终会调用`EventPublishingRunListener`的`contextPrepared`方法
- `EventPublishingRunListener`就是一个事件发布的工具类，通过工厂加载机制获得后，封装到`SpringApplicationRunListeners`中。
- `SpringApplicationRunListeners`中保存的是`SpringApplicationRunListener`的集合，每个方法都是遍历调用。

```java
// EventPublishingRunListener
@Override
public void contextPrepared(ConfigurableApplicationContext context) {
    // 还是调用广播器广播的事件
    this.initialMulticaster
        .multicastEvent(new ApplicationContextInitializedEvent(this.application, this.args, context));
}
```

- debug发现此处包含两个监听器
  1. BackgroundPreinitializer
  2. DelegatingApplicationListener





## Load(Application,Object[])

```java
// SpringApplication
// 入参为刚创建的应用上下文以及sources(包含primarySources和sources)
protected void load(ApplicationContext context, Object[] sources) {
    if (logger.isDebugEnabled()) {
        logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
    }
    // 创建一个BeanDefinitionLoader
    BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
    // 设置加载器的一些属性
    if (this.beanNameGenerator != null) {
        loader.setBeanNameGenerator(this.beanNameGenerator);
    }
    if (this.resourceLoader != null) {
        loader.setResourceLoader(this.resourceLoader);
    }
    if (this.environment != null) {
        loader.setEnvironment(this.environment);
    }
    // 遍历加载sources
    loader.load();
}

// SpringApplication
// 从上下文中获取BeanDefinitionRegistry
// 根据不同的类型有不同的获取方式
private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
    if (context instanceof BeanDefinitionRegistry) {
        return (BeanDefinitionRegistry) context;
    }
    if (context instanceof AbstractApplicationContext) {
        return (BeanDefinitionRegistry) ((AbstractApplicationContext) context).getBeanFactory();
    }
    throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
}

// SpringApplication
protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
    return new BeanDefinitionLoader(registry, sources);
}

// BeanDefinitionLoader
BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
    Assert.notNull(registry, "Registry must not be null");
    Assert.notEmpty(sources, "Sources must not be empty");
    this.sources = sources;
    // 初始化AnnotatedBeanDefinitionReader
    this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
    this.xmlReader = new XmlBeanDefinitionReader(registry);
    if (isGroovyPresent()) {
        this.groovyReader = new GroovyBeanDefinitionReader(registry);
    }
    this.scanner = new ClassPathBeanDefinitionScanner(registry);
    this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
}

```



## contextLoaded

- 发布`ApplicationPreparedEvent`事件
- 另外在`EventPublishingRunListener`中可以看到一些额外处理：
  - 如果`listener`继承`ApplicationContextAware`，那么此时就会吧`context`注入到`listener`中。
  - 将`listener`添加到上下文中

```java
// SpringApplicationRunListeners
void contextLoaded(ConfigurableApplicationContext context) {
    for (SpringApplicationRunListener listener : this.listeners) {
        listener.contextLoaded(context);
    }
}

// EventPublishingRunListener
@Override
public void contextLoaded(ConfigurableApplicationContext context) {
    for (ApplicationListener<?> listener : this.application.getListeners()) {
        if (listener instanceof ApplicationContextAware) {
            ((ApplicationContextAware) listener).setApplicationContext(context);
        }
        context.addApplicationListener(listener);
    }
    this.initialMulticaster.multicastEvent(new ApplicationPreparedEvent(this.application, this.args, context));
}
```



# 总结

上下文准备方法的主要作用：

1. 将之前`SpringApplication`中的环境填充到上下文中
2. 调用之前加载的`ApplicationContextInitializer`方法
3. 发布`ApplicationContextInitializedEvent`
4. 将命令行参数以及`banner`注册为Bean对象
5. 创建BeanDefiniionLoader，并加载资源对象
6. 发布ApplicationPreparedEvent，并将监听器对象添加到上下文中