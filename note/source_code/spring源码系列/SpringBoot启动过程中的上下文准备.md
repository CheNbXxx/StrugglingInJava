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
		load(context, sources.toArray(new Object[0]));
		listeners.contextLoaded(context);
	}
```



## Load(Application,Object[])

```java
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
    loader.load();
}
```



