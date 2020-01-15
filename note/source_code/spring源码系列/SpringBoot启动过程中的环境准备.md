# SpringBoot启动流程中的环境配置



- run方法中的环境准备只有一行代码，但是点开之后有点多的。

```java
ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
```



## Environment类族

严格来说应该是`PropertyResolver`类族，因为`Environment`继承自`PropertyResolver`。

`PropertyResolver`提供了对Property属性的访问方式，`Environment`在此基础上提供了对Profiles属性的访问。

Property可以简单理解为键值对属性，而Profiles则是有效的配置文件。

以上两个接口提供了getter方法，另外和`Environment`同级的`ConfigurablePropertyResolver`，提供了对一些属性的setter方法，类型转换的功能。

以上是三个高级的接口抽象。

`AbstractEnvironment`中规定了保存两种属性的基本数据结构

```java
// AbstractEnvironment
// 两种Profiles都是一LinkedHashSet保存的
private final Set<String> activeProfiles = new LinkedHashSet<>();
private final Set<String> defaultProfiles = new LinkedHashSet<>(getReservedDefaultProfiles());
// PropertySource就是对k,v的一个包装，MutablePropertySources是对PropertySources集合的一个包装
// mps里面包含一个CopyOrWriteList集合
private final MutablePropertySources propertySources = new MutablePropertySources();
```

`AbstractPropertyResolver`则是属性解析的基类。

<font size=2>（避免代码过多，非主要逻辑不贴代码）</font>

## prepareEnvironment方法

```java
// SpringApplication
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
      ApplicationArguments applicationArguments) {
    // 获取或者创建容器环境
    // SpringBoot中的容器环境使用Environment表示
   ConfigurableEnvironment environment = getOrCreateEnvironment();
    // 配置环境，创建之后根据传入参数对环境对象的配置
   configureEnvironment(environment, applicationArguments.getSourceArgs());
    // 将资源本身封装成一个元素放在列表头,调用了两次暂时不知道具体作用
   ConfigurationPropertySources.attach(environment);
    // 触发ConfigFileApplicationListener，加载如application.yml的配置文件
   listeners.environmentPrepared(environment);
    // 将环境绑定到当前的容器上下文
   bindToSpringApplication(environment);
   if (!this.isCustomEnvironment) {
      environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
            deduceEnvironmentClass());
   }
   ConfigurationPropertySources.attach(environment);
   return environment;
}
```



### 创建容器环境

- 根据不同的应用类型创建不同的环境类型。
- 常用的servlet使用StandardservletEnvironment类作为环境。

```java
	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
			return this.environment;
		}
		switch (this.webApplicationType) {
		case SERVLET:
			return new StandardServletEnvironment();
		case REACTIVE:
			return new StandardReactiveWebEnvironment();
		default:
			return new StandardEnvironment();
		}
	}
```



### 配置环境

- 配置创建的Environment对象
- 主要包含Conversionservice，PropertySource以及Profiles的配置。

```java
protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
	if (this.addConversionService) {
        // 获取共享的ApplicationConversionService对象
        // 使用的双重校验的
		ConversionService conversionService = ApplicationConversionService.getSharedInstance();
		environment.setConversionService((ConfigurableConversionService) conversionService);
	}
	configurePropertySources(environment, args);
	configureProfiles(environment, args);
}
```



PropertySource的相关配置代码

```java
	protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
        // propertySource就是环境中的配置源
		MutablePropertySources sources = environment.getPropertySources();
        // 配置默认的属性
		if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
			sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
		}
        // 配置命令行参数
        // 都是包装成SimpleCommandLinePropertySource
		if (this.addCommandLineProperties && args.length > 0) {
			String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
            // 如果存在则加入到原命令行参数的集合中
			if (sources.contains(name)) {
				PropertySource<?> source = sources.get(namApplicationEnvironmentPreparedEvente);
				CompositePropertySource composite = new CompositePropertySource(name);
				composite.addPropertySource(
						new SimpleCommandLinePropertySource("springApplicationCommandLineArgs", args));
				sources.replace(name, composite);
			}
			else {
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
	}
```



### 附加属性配置

- 环境中的propertySources属性会在其内存一份自身作为k/v属性。
- prepareEnvironment方法中调用了两次，暂时还不知道什么用处。

```java
	public static void attach(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        // 获取环境中的propertySources
				composite.addPropertySource(source);调用了两边，
		MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
        // propertySources中的附加属性源属性
		PropertySource<?> attached = sources.get(ATTACHED_PROPERTY_SOURCE_NAME);
		if (attached != null && attached.getSource() != sources) {
			sources.remove(ATTACHED_PROPERTY_SOURCE_NAME);
			attached = null;
		}
		if (attached == null) {
			sources.addFirst(new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME,
					new SpringConfigurationPropertySources(sources)));
		}
	}
```



### 触发ApplicationEnvironmentPreparedEvent

ApplicationEnvironmentPreparedEvent在监听器中会加载yml和properties文件中的配置。

此处会触发包含`ConfigFileApplicationListener`在内的七个监听器。

逻辑有点复杂，记账，下次再说。



### 绑定环境

将准备好的容器环境绑定到当前的上下文。

```java
	protected void bindToSpringApplication(ConfigurableEnvironment environment) {
		try {
            // get方法是以environment为基准获取Binder对象
            // Bindable.ofInstance(this)
			Binder.get(environment).bind("spring.main", Bindable.ofInstance(this));
		}catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}
```

点开好像还是很复杂，记账记账。



## 总结

`prepareEnvironment`方法的主要作用就是准备环境。

1. 创建环境类实例
2. 添加命令行参数等一些配置到实例中
3. 触发`ApplicationEnvironmentPreparedEvent`，读取配置文件到环境中
4. 将创建好的环境对象与当前的SpringApplication对象绑定