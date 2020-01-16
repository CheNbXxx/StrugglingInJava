# SpringBoot的启动流程概述

- 尽量不会有太多的代码，以理清楚流程为主
- 因为一行代码点进去可能就是几百几千行代码，一次分析完太累了。

# 启动类调用

```java

@SpringBootApplication
public class BeanValidationBootStrap {
    public static void main(String[] args) {
        SpringApplication.run(BeanValidationBootStrap.class, args);
    }
}
```

以上是最基础的启动类代码，调用SpringApplication的静态方法run启动Spring的整个容器。

# SpringApplication构造函数

```java
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // 资源加载器,此处为null
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null";
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

- mainApplicationClass的推断过程很有意思，直接构造一个RuntimeException然后遍历异常的堆栈信息查找main方法，获取当前主类。

```java
	private Class<?> deduceMainApplicationClass() {
		try {
			StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				if ("main".equals(stackTraceElement.getMethodName())) {
					return Class.forName(stackTraceElement.getClassName());
				}
			}
		}catch (ClassNotFoundException ex) {
			// Swallow and continue
		}
		return null;
	}
```



- 配置主要资源
- 推断web应用类型
- 通过工厂加载机制加载应用上下文初始化器（ApplicationContextInitializer）和应用监听者（ApplicationListener）
- 推断应用主类

# Run()

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
        // 获取SpringApplicationRunListener，并封装为一个对象
		SpringApplicationRunListeners listeners = getRunListeners(args)；
         // 触发ApplicationStartingEvent
		listeners.starting();
		try {
            // 包装传入的应用参数
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 准备容器环境
            // 会触发ApplicationEnvironmentPreparedEvent，读取配置文件
            // 创建并配置Environment对象，并绑定到当前的应用上下文
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
		return context;
	}
```



## 获取并启动监听器

- Spring中的监听器采用的都是观察者模式。
- 此时会创建`EventPublishingRunListener`实例，并将`SpringApplication`中的Listeners导入到期内的广播器中。

```java
// SpringApplication	
private SpringApplicationRunListeners getRunListeners(String[] args) {
    Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
    return new SpringApplicationRunListeners(logger,
                                             getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
}

// SpringApplication	
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
    // 获取类加载器
    ClassLoader classLoader = getClassLoader();
    // 代码很熟悉，依旧是使用工厂加载模式获取spring.factories文件中配置的SpringApplicationRunListener子类实现
    Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
    List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
    AnnotationAwareOrderComparator.sort(instances);
    return instances;
}

// SpringApplication
private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
                                                   ClassLoader classLoader, Object[] args, Set<String> names) {
    List<T> instances = new ArrayList<>(names.size());
    // 按照获取的对象名称，遍历初始化
    for (String name : names) {
        try {
            Class<?> instanceClass = ClassUtils.forName(name, classLoader);
            Assert.isAssignable(type, instanceClass);
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
            T instance = (T) BeanUtils.instantiateClass(constructor, args);
            instances.add(instance);
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, ex);
        }
    }
    return instances;
}
```



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
		// 获取所有的配置，包含SpringApplication中的sources和primarySources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
         // 发布ApplicationPreparedEvent
		listeners.contextLoaded(context);
	}

```



## 刷新上下文

```java
    // AbstractApplicationContext 
    @Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
				initMessageSource();

				// Initialize event multicaster for this context.
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
			}
		}
	}
```



# 补充

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

