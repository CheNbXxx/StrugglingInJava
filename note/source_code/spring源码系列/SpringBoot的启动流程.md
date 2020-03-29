# SpringBoot的启动流程概述

- 尽量不会有太多的代码，以理清楚流程为主
- 因为一行代码点进去可能就是几百几千行代码，一次分析完太累了。

## 启动类调用

```java
@SpringBootApplication
public class BeanValidationBootStrap {
    public static void main(String[] args) {
        SpringApplication.run(BeanValidationBootStrap.class, args);
    }
}
```

以上是最基础的启动类代码，调用SpringApplication的静态方法run启动Spring的整个容器。

## SpringApplication构造函数

```java
// 直接点进来的话，这个ResourceLoader是null	
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // 资源加载器,此处为null
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null");
         // 主要数据源集合
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
         // Web应用类型
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
         // 设置初始化器
	     setInitializers(
            (Collection)getSpringFactoriesInstances(ApplicationContextInitializer.class));
          // 设置监听者
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
          // 推断应用主类，此处代码我感觉还是很新奇的
		this.mainApplicationClass = deduceMainApplicationClass();
	}
```

### 初始化器

ApplicationContextInitializer的实现子类:

 ![image-20200329145817718](/home/chen/github/StrugglingInJava/pic/image-20200329145817718.png)

ServerPortInfoApplicationContextInitializer 会直接添加一个WebServerInitializedEvent的监听

### 监听器

 ![image-20200329145919656](/home/chen/github/StrugglingInJava/pic/image-20200329145919656.png)

其中最为关键的应该就是ConfigFileApplicationListener,会响应ApplicationEnvironmentPreparedEvent和ApplicationPreparedEvent事件,加载各类配置文件.

### 推断主类

mainApplicationClass的推断过程很有意思，直接构造一个RuntimeException然后遍历异常的堆栈信息查找main方法，获取当前主类。

```java
...
    try {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if ("main".equals(stackTraceElement.getMethodName())) {
                return Class.forName(stackTraceElement.getClassName());
            }
        }
 ...
```



---

# Run()方法

- run方法是启动的核心方法，包含了环境准备，监听事件的发布，上下文的刷新及后续处理等等。

```java
	public ConfigurableApplicationContext run(String... args) {
        // 用于记录时间，可以当做是秒表
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
        // Headless相关配置
		configureHeadlessProperty();
        // 工厂加载机制获取SpringApplicationRunListener，并封装为一个对象
		SpringApplicationRunListeners listeners = getRunListeners(args)；
         // 触发ApplicationStartingEvent
		listeners.starting();
		try {
            // 对main方法的入参进行包装
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 准备容器环境
            // 会触发ApplicationEnvironmentPreparedEvent，读取配置文件中的内容
        	// 会将环境与当前的SpringApplication绑定
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
            // 配置忽略的Bean信息,`spring.beaninfo.ignore`配置项
			configureIgnoreBeanInfo(environment);
            // 输出Banner
			Banner printedBanner = printBanner(environment);
            // 创建对应的应用上下文
			context = createApplicationContext();
            // 还是工厂加载模式，获取异常的报告之类的
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
            // 准备上下文 
			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
            // 刷新上下文
			refreshContext(context);
            // 刷新上下文之后的操作
			afterRefresh(context, applicationArguments);
            // 计时结束
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;args
	}
```



## 获取并启动监听器

```java
SpringApplicationRunListeners listeners = getRunListeners(args)；
      
private SpringApplicationRunListeners getRunListeners(String[] args) {
    Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
    return new SpringApplicationRunListeners(logger,
                                             getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
}
```

Spring中的事件发布一般是通过`ApplicationContext`实现,但是此时并没有准备好应用上下文,所以会以`SpringApplicationRunListeners`的特殊工具类发布.

`SpringApplicationRunListeners`内部封装了Log对象和`SpringApplicationRunListener`的集合.

`SpringApplicationRunListener`是对启动过程中事件发布的规范接口,定义了各种相关事件.

默认的实现只有`EventPublishingRunListener`.

```java
// EventPublishingRunListener的构造函数
public EventPublishingRunListener(SpringApplication application, String[] args) {
    this.application = application;
    this.args = args;
    this.initialMulticaster = new SimpleApplicationEventMulticaster();
    for (ApplicationListener<?> listener : application.getListeners()) {
        this.initialMulticaster.addApplicationListener(listener);
    }
}
```

从构造函数也可以看出,`EventPublishingRunListener`就是对广播器的一个封装,事件广播最终还是会通过`SimpleApplicationEventMulticaster`.



## 发布ApplicationStartingEvent

该事件涉及的监听器有以下四个:

 ![image-20200329152842427](/home/chen/github/StrugglingInJava/pic/image-20200329152842427.png)

具体作用先忽略.



## 环境准备

[SpringBoot启动过程中的环境准备](./SpringBoot启动过程中的环境准备)



## 创建应用上下文

逻辑很简单，根据不同的Web应用类型创建对应的上下文类

- Default - `AnnotationConfigApplicationContext`
- Servlet - `AnnotationConfigServletWebServerApplicationContext`
- Reactive - `AnnotationConfigReactiveWebServerApplicationContext`



## 准备上下文

- 应用`ApplicationContextInitializer`
- 上下文准备完成事件
- 注册特定的一些单例Bean对象（包‘括banner）
- 加载应用上下文
- 上下文加载完成事件

```java
	private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
        // 设置环境
        // 不仅仅配置到上下文，也会配置到上下文中的BeanDefinitionReader和Scanner
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
        // 应用所有的ApplicationContextInitializer类
        // 此时的类都是在Application的构造函数中通过工厂加载机制获取的
		applyInitializers(context);
        // 发布ApplicationContextInitializedEvent
		listeners.contextPrepared(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		// 塞几个特定的bean
        // spring的启动参数bean
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        // 命令行参数的Bean
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
             // banner为啥也要塞进来。。。。
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
        // 设置是否允许bean对象的覆盖
        // 默认是false
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		if (this.lazyInitialization) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		// 获取所有的配-置，包含SpringApplication中的sources和primarySources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
         // 发布ApplicationPreparedEvent
		listeners.contextLoaded(context);
	}

```

# 

## 1.ApplicationContextInitializer  -  引用上下文初始化

```java
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {
	/**
	 * Initialize the given application context.
	 * @param applicationContext the application to configure
	 */
	void initialize(C applicationContext);
}
```

在refresh方法之前会调用的回调方法。

以`ConfigurableApplicationContext`为入参，可以对其进行修改。

可以使用`@Order`或者Ordered接口进行排序。

