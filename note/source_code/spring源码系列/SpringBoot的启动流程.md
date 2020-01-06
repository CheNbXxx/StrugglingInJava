# Application

```java

@SpringBootApplication
public class BeanValidationBootStrap {
    public static void main(String[] args) {
        SpringApplication.run(BeanValidationBootStrap.class, args);
    }
}
```

以上是最基础的启动类代码，调用SpringApplication的静态方法run启动Spring的整个容器。

# 总流程

1. SpringApplication的构造函数
   1. 推断应用类型
   2. 推断主类
   3. 设置ApplicationContextInitializer
   4. 设置ApplicationListener
2. Application.run方法
   1. 



# run方法前瞻

```java
	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        // run方法直接调用的另外一个run重载
		return run(new Class<?>[] { primarySource }, args);
	}

    public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        // 首先调用的Application的构造函数
		return new SpringApplication(primarySources).run(args);
	}
```



# Application的构造函数

SpringApplication的构造函数主要就是推断并设置一些基础参数。

入参PrimarySource为主类的类对象。

```java
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // 资源读取工具
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null";
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
         // Web应用类型
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
         // 设置初始化器
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
          // 设置监听者
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
          // 推断应用主类，此处代码我感觉还是很新奇的
		this.mainApplicationCl代码如下：ass = deduceMainApplicationClass();
	}
```

## 推断Web应用类型

代码如下：

首先SpringBoot的Web应用类型有三种：

NONE，SERVLET，REACTIVE

```java
private static final String[] SERVLET_INDICATOR_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";

	private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

	private static final String SERVLET_APPLICATION_CONTEXT_CLASS = "org.springframework.web.context.WebApplicationContext";

	private static final String REACTIVE_APPLICATION_CONTEXT_CLASS = "org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext";
	static WebApplicationType deduceFromClasspath() {
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}
		return WebApplicationType.SERVLET;
	}
```

1. 存在DispatcherHandler并且不存在DispatcherServlet和ServletContainer，便判断为REACTIVE类型。
2. Servlet和ConfigurableWebApplicationContext有一个不存在便可以判定为非WEB类型。
3. 其他全部归于SERVLET类型。

## 推断应用主类

代码如下：	private SpringApplicationRunListeners getRunListeners(String[] args) {
		Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
		return new SpringApplicationRunListeners(logger,
				getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
	}

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

直接手动创建异常，通过遍历异常的堆栈信息查找main函数的类，并加载。



# Run的主方法

```java
	public ConfigurableApplicationContext run(String... args) {
        // StopWatch好像是用于记录时间
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
        // Headless相关配置
		configureHeadlessProperty();
        // 获取SpringApplicationRunListeners并启动
		SpringApplicationRunListeners listeners = getRunListeners(args)；
		listeners.starting();
		try {
            // 包装传入的应用参数
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 准备环境，结束后会广播环境准备完成事件
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
            // 配置忽略的Bean信息
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
            // 创建对应的应用上下文
			context = createApplicationContext();
            // 还是工厂加载模式，获取异常的报告之类的
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
            // 准备上下文 传入了刚创建的上下文对象，环境，监听以及参数，还有banner
			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
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



## 获取应用启动监听者

主要还是工厂加载模式，加载对象为SpringApplicationRunListener类型的对象

```java
	private SpringApplicationRunListeners getRunListeners(String[] args) {
		Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
		return new SpringApplicationRunListeners(logger,
                 // 工厂模式,外层封装为SpringApplicationRunListeners对象
				getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
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

```java
	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
            // 如果环境已经存在不会销毁重建
			return this.environment;
		}
		switch (this.webApplicationType) {
		case SERVLET:
			return new StandardServletEnvironment();
		case REACTIVE:
			return new StandardReactiveWebEnvironment();
		default:}
		}
			return new StandardEnvironment();
		}
	}
```



### 应用环境参数

- 分为PropertySource和Profiles分别在两个方法中配置

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
        // 就是run方法传入的所有args,包装成ApplicationArguments 对象后，此处解析
		if (this.addCommandLineProperties && args.length > 0) {
            // 如果已经存在就添加到命令行参数里面
			String name = CommandLiveProfiles(Str * Interface for resolving properties against any underlying source.
ingUtils.toStringArray(profiles));
nePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
			if (sources.contains(name)) {
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



#### 设置活跃的配置文件

```java
this.defaultBindHandler;  // 设置活跃的配置文件 spring.profiles.active
	protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
		Set<String> profiles = new LinkedHashSet<>(this.additionalProfiles);
		profiles.addAll(Arrays.asList(environment.getActiveProfiles()));
		environment.setActiveProfiles(StringUtils.toStringArray(profiles));
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

根据不同的Web应用类型创建对应的上下文类

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

```java
	private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
        // 设置环境，简单set
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
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
		listeners.contextLoaded(context);
	}
```

