# SpringBoot启动过程中的上下文准备





<!-- more -->

---

[TOC]



## prepareContext -主方法

```java
	private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
            // 填充环境到应用上下文
            context.setEnvironment(environment);
            // 应用上下文的后续处理,具体可看下文
            postProcessApplicationContext(context);
            // 应用全部的初始化器
            applyInitializers(context);
            // 发布ApplicationContextInitializedEvent
            listeners.contextPrepared(context);
            if (this.logStartupInfo) {
                    logStartupInfo(context.getParent() == null);
                    logStartupProfileInfo(context);
            }
            // 获取beanFactory对象
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            // 将args的包装对象注册为单例
            beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
            // 将Banner也注册了
            if (printedBanner != null) {
                	beanFactory.registerSingleton("springBootBanner", printedBanner);
            }
            // 判断如果是DefaultListableBeanFactory类
            if (beanFactory instanceof DefaultListableBeanFactory) {
               		 // 设置BeanDefinition是否可覆盖
                    ((DefaultListableBeanFactory) beanFactory)
                            // allowBeanDefinitionOverriding在环境准备的bind()方法中修改
                            // 通过调用链得知,具体省略
                            .setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
            }
            // 如果是懒加载增加一个BeanFactoryPostProcessor
 			if (this.lazyInitialization) {
					context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
			}
            // Load the sources
            // 获取要加载的全部源信息
            Set<Object> sources = getAllSources();
            Assert.notEmpty(sources, "Sources must not be empty");
            // 加载BeanDefinitionLoader
            load(context, sources.toArray(new Object[0]));
            listeners.contextLoaded(context);
	}
```

### postProcessApplicationContext - 应用上下文的后处理

```java
public static final String CONFIGURATION_BEAN_NAME_GENERATOR =
			"org.springframework.context.annotation.internalConfigurationBeanNameGenerator";

// SpringApplication	
protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
            // 注册BeanNameGenerator为单例Bean 
            if (this.beanNameGenerator != null) {
                context.getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
                                                           this.beanNameGenerator);
            }
            // 配置资源加载器，以及类加载器
            if (this.resourceLoader != null) {
                	if (context instanceof GenericApplicationContext) {
                    		((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
                	}
                	if (context instanceof DefaultResourceLoader) {
                    		((DefaultResourceLoader) context).setClassLoader(this.resourceLoader.getClassLoader());
                	}
            }
            // 填充转换类到BeanFactory
        	if (this.addConversionService) {
                	context.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
            }
	}
```

具体逻辑不复杂,总共做的事情如下

1. 注册了beanNameGenerator的单例Bean
2. 配置上下文中的SourceLoader和ClassLoader
3. 配置ConversionService

`BeanNameGenerator`听名字就是到是为了生成Bean的名称的。

这里为什么要区分ApplicationContext的类型来配置SourceLoader和ClassLoader，原因待定。



### applyInitializers - 应用初始化器

此方法内调用在构造函数中填充的全部初始化器的`initialize`方法.

```java
// SpringApplication
protected void applyInitializers(ConfigurableApplicationContext context) {
         // 遍历调用
        for (ApplicationContextInitializer initializer : getInitializers()) {
            // 判断类型
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
                                                                            ApplicationContextInitializer.class);
            Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
    		// 调用
            initializer.initialize(context);
        }
}
// 就是将一开始构造函数中获取的初始化器全部提取并排序
public Set<ApplicationContextInitializer<?>> getInitializers() {
   	 	return asUnmodifiableOrderedSet(this.initializers);
}
```



###  listeners.contextPrepared - 发布ApplicationContextInitializedEvent

以下为具体的监听者

 ![image-20200414232431965](../../../../pic/image-20200414232431965.png)

功能先忽略.



### BeanFactory相关配置

```java
 // 获取beanFactory对象
ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
// 将args的包装对象注册为单例
beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
// 将Banner也注册了
if (printedBanner != null) {
    	beanFactory.registerSingleton("springBootBanner", printedBanner);
}
// 判断如果是DefaultListableBeanFactory类
if (beanFactory instanceof DefaultListableBeanFactory) {
        // 设置BeanDefinition是否可覆盖
        // allowBeanDefinitionOverriding在环境准备的bind()方法中修改
        // 通过调用链得知,具体省略
        ((DefaultListableBeanFactory) beanFactory) .setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
}
// 如果是懒加载增加一个BeanFactoryPostProcessor
if (this.lazyInitialization) {
    	context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
}
```

在上下文准备中的主方法中的环境配置：

1. 注册运行时参数为单例的Bean对象
2. 注册Banner为单例Bean对象
3. 配置是否允许BeanDefinition的覆盖，因为之后要加载BeanDefinition了，所以此处先判断。
4. 配置LaztInitializationBeanFactoryPostProcessor

3,4都是条件配置，BeanFactoryPostcProcessor在上下文刷新值统一执行。





### 加载BeanDefinition

```java
 // Load the sources
// 获取要加载的全部源信息
Set<Object> sources = getAllSources();
Assert.notEmpty(sources, "Sources must not be empty");
// 加载BeanDefinitionLoader
load(context, sources.toArray(new Object[0]));
```

这应该是上下文准备阶段最复杂的步骤了。

直接从load方法开始看吧。



#### getAllSources - 获取所有配置源

```java
// SpringApplictaion
// 获取所有的源信息，组合主要源和附加的
public Set<Object> getAllSources() {
        Set<Object> allSources = new LinkedHashSet<>();
    	// primarySources是在SpringApplication的构造函数里面就填充了
        if (!CollectionUtils.isEmpty(this.primarySources)) {
            	allSources.addAll(this.primarySources);
        }
        if (!CollectionUtils.isEmpty(this.sources)) {
            	allSources.addAll(this.sources);
        }
        return Collections.unmodifiableSet(allSources);
}
```

所谓的AllSource就是配置的primarySources和sources。



#### load - 加载源信息

```java
// SpringApplictaion
protected void load(ApplicationContext context, Object[] sources) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
            }
    		// 创建BeanDefinitionLoader类
            BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
    		// 为BeanDefinitionLoader填充属性
            if (this.beanNameGenerator != null) {
                loader.setBeanNameGenerator(this.beanNameGenerator);
            }
            if (this.resourceLoader != null) {
                loader.setResourceLoader(this.resourceLoader);
            }
            if (this.environment != null) {
                loader.setEnvironment(this.environment);
            }
    		// 加载BeanDefinitionLoader
            loader.load();
}

```

方法具体逻辑如下：

1. 创建BeanDefinitionLoader
2. 配置BeanDefinitionLoader
3. 使用BeanDefinitionLoader

配置的时候会配置BeanNameGenerator和resourceLoader，这些在之前的postProcessApplicationContext方法中都已经被添加到SpringApplication中。

之后就是load的方法，也是加载BeanDefinition的主要方法。

看到这方法名最头疼，之前`ConfigFileApplicationListener`中加载配置文件也是load方法，加上各种重载，lambda，头看瞎了。

点开BeanDefinitionLoader的类，这一溜的load方法，呵呵呵。

具体加载过程可见：

[BeanDefinitionLoader](../../BeanDefinitionLoader.md)



## 发布ApplicationPreparedEvent

ApplicationPreparedEvent的发布流程和其他的不同，这里单独记一下。

SpringBoot的启动阶段的事件发布的调用链都会指到SpringApplicationRunListener。

```java
// EventPublisherRunListener	
@Override
public void contextLoaded(ConfigurableApplicationContext context) {
    	// 将监听器和ApplicationContext绑定
        for (ApplicationListener<?> listener : this.application.getListeners()) {
                if (listener instanceof ApplicationContextAware) {
                  	  ((ApplicationContextAware) listener).setApplicationContext(context);
                }
                context.addApplicationListener(listener);
        }
    	// 事件发布
        this.initialMulticaster.multicastEvent(new ApplicationPreparedEvent(this.application, this.args, context));
}
```

**到ApplicationPreparedEvent之后，监听器已经和ApplicationContext绑定，所以之后的发布都会通过ApplicationContext完成**.



触发的对应监听器有以下五个：

 ![image-20200415101613282](../../../../pic/image-20200415101613282.png)