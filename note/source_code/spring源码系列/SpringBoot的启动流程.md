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

构造函数中完成了基本属性配置

- primarySource为主类的类对象。
- 使用工厂加载机制获取需要的ApplicationContextInitializer和ApplicationListener。
- 推断Web类型以及主类对象。
- Web类型有三种 - NONE，SERVLET，REACTIVE

```java
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // 资源加载器
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null";
         // 主要数据源集合
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
         // Web应用类型
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
         // 设置初始化器
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
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
		}
		catch (ClassNotFoundException ex) {
			// Swallow and continue
		}
		return null;
	}
```



# Run方法

- run方法是启动的核心方法，包含了环境准备，监听事件的发布，上下文的刷新及后续处理等等。

```java
	public ConfigurableApplicationContext run(String... args) {
        // StopWatch好像是用于记录时间
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
        // 因为try...catch之外会用到，所以在外面先声明
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
            // 配置忽略的Bean信息
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





## 准备容器环境

```java
	private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments) {
		// Create and configure the environment
       // 获取或创建当前容器环境
        // 简单的判断是否已经创建，或者根据webApplicationType新建环境对象
		ConfigurableEnvironment environment = getOrCreateEnvironment();
        // 将命令行参数绑定到环境中
		configureEnvironment(envgetSpringFactoriesInstancesironment, applicationArguments.getSourceArgs());
        // 重新绑定environment，修改environment的propertySources元素
		ConfigurationPropertySources.attach(environment);
        // 广播配置准备完成事件
		listeners.environmentPrepared(environment);
        // 绑定到应用上下文，配置文件中的一些配置此时生效
		bindToSpringApplication(environment);
		if (!this.isCustomEnvironment) {
			environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
					deduceEnvironmentClass());
		}
        // 第二次调用
		ConfigurationPropertySources.attach(environment);
		return environment;
	}
```

### 创建容器环境

环境是根据主类类型配置的：

Servlet - StandardServletEnvironment        // 重新绑定environment，修改environment的propertySources

environmentPreparedReactive - StandardReactiveWebEnvironment

Default - StandardEnvironment



### 配置环境

环境配置主要分为三步：

1. 配置ConversionService
2. 配置PropertySources，包含默认参数以及命令行参数
3. 配置活跃的Profiles

```java
	protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
        // 设置ApplicationConversionService
		if (this.addConversionService) {
			ConversionService conversionService = ApplicationConversionService.getSharedInstance();
			environment.setConversionService((ConfigurableConversionService) conversionService);
        }
		configurePropertySources(environment, args);
		configureProfiles(environment, args);
	}
```



#### PropertySource

- 此时加入默认参数以及命令行参数

```java
public static final String COMMAND_LINE_PROPERTY_SOURCE_NAME = "commandLineArgs";

protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
       // 环境中的存放配置的容器是MutablePropert * Interface foATTACHED_PROPERTY_SOURCE_NAMEr resolving properties against any underlying source.
ySourcesenvironmentPrepared
     // 以为是个map，其实里面是一个CopyOnWriteArrayList
       // List中的PropertySource是个{Stirng：name，T source}结构
    // PropertySource类族以后再说吧
		MutablePropertySources sources = environment.getPropertySources();
		if (this.defaultProperties != null && !this.defaultProper知否知否 胡夏ties.isEmpty()) {
            // 添加默认配置
			sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
		}
        // 命令行属性配置
        // 就是run方法传入的所有args,包装成ApplicationArguments 对象后，此处解析		if (this.addCommandLineProperties && args.length > 0) {
            // 如果已经存在就添加到命令行参数里面
			String name = CommandLiveProfiles(Str * Interface for resolving properties against any underlying source.
ingUtils.toStringArray(profiles));
nePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
			if (sources.contains(name)) {SimpleCommandLinePropertySource
				PropertySource<?> source = sources.get(name);
				CompositePropertySource composite = new CompositePropertySource(name);
				composite.addPropertySource(
						new SimpleCommandLinePropertySource("springApplicationCommandLineArgs", args));
				composite.addPropertySource(source);
				sources.replace(name, composite);
			}
			else {
                // 添加整个类型的参数
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
	}
```



### 将当前的配置

- 将PropertySource和环境（environment中的propertySource属性）绑定

1. 从当前环境中获取PropertySource属性
2. 从source中获取configurationProperties属性
3. 若属性不为空且于当前要设置

```java
private static final String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

public static void attach(Environment environment) {
   Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
    // 获取原环境的propertySources属性
   MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
    // 从sources中获取configurationProperties的配置项
    // 传入名称之后的比较都是通过将其包装为ComparisonPropertySource
   PropertySource<?> attached = sources.get(ATTACHED_PROPERTY_SOURCE_NAME);
   // 获取的环境不为空且value和当前sources不同
   if (attached != null && attached.getSource() != sources) {
      sources.remove(ATTACHED_PROPERTY_SOURCE_NAME);
      attached = null;
   }
    // 添加新的this.defaultBindHandler;
   if (attached == null) {environmentPrepared
       //  将其包装为string-sources的ConfigurationPropertySourcesPropertySource存放
      sources.addFirst(new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME,
            new SpringConfigurationPropertySources(sources)));
   }
}
```



### 将环境配置绑定到当前的应用上下文

```java
	protected void bindToSpringApplication(ConfigurableEnvironment environment) {
		try {
            // md，看着一行方法拆解开来一大堆 
            //  深入之后这个环境到Application的绑定重载太多了，以后再看吧，先过一遍整体流程
            Binder.get(environment).bind("spring.main", Bindable.ofInstance(this));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
    }
```



## 创建应用上下文

逻辑很简单，根据不同的Web应用类型创建对应的上下文类

- Default - AnnotationConfigApplicationContext
- Servlet - AnnotationConfigServletWebServerApplicationContext
- Reactive - AnnotationConfigReactiveWebServerApplicationContext

```java
	public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
			+ "annotation.AnnotationConfigApplicationContext";
	public static final String DEFAULT_SERVLET_WEB_CONTEXT_CLASS = "org.springframework.boot."
			+ "web.servlet.context.AnnotationConfigServletWebServerApplicationContext";
	public static final String DEFAULT_REACTIVE_WEB_CONTEXT_CLASS = "org.springframework."
			+ "boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext";
protected ConfigurableApplicationContext createApplicationContext() {
		Class<?> contextClass = this.applicationContextClass;
		if (contextClass == null) {
			try {
				switch (this.webApplicationType) {
				case SERVLET:
					contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
					break;
				case REACTIVE:
					contextClass = Class.forName(DEFAULT_REACTIVE_WEB_CONTEXT_CLASS);
					break;
				default:
					contextClass = Class.forName(DEFAULT_CONTEXT_CLASS);
				}
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Unable create a default ApplicationContext, please specify an ApplicationContextClass", ex);
			}
		}
        //  实例化应用上下文类 
		return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
	}
```



## 准备上下文

- 应用`ApplicationContextInitializer`
- 上下文准备完成事件
- 注册特定的一些单例Bean对象（包括banner）
- 加载应用上下文
- 上下文加载完成事件

```java
	private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
        // 设置环境
        // 并不是简单的set，我们此时的context是AnnotationConfigServletWebServerApplicationContext
        // 此时还会设置两个beanDefintion的读取类
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
        // 应用所有的ApplicationContextInitializer类
        // 此时的类都是在Application的构造函数中通过工厂模式加载的
        // 是Spring的扩展点之一
		applyInitializers(context);
        // 广播上下文准备完成的事件
		listeners.contextPrepared(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		// 塞几个特定的bean
        // spring的启动参数bean
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
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
		// 获取所有的配置
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
         // 广播上下文加载完毕的事件
		listeners.contextLoaded(context);
	}

	// SpringApplication
	public Set<Object> getAllSources() {
        // primarySources就是从启动类调用时传入的主类集合
		Set<Object> allSources = new LinkedHashSet<>();
		if (!CollectionUtils.isEmpty(this.primarySources)) {
			allSources.addAll(this.primarySources);
		}
		if (!CollectionUtils.isEmpty(this.sources)) {
			allSources.addAll(this.sources);
		}
		return Collections.unmodifiableSet(allSources);
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

