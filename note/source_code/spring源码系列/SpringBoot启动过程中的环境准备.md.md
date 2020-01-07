# SpringBoot启动流程中的环境配置



- run方法中的环境准备只有一行代码，但是点开之后有点多的。

```java
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
```



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
   ConfigurationPropertySources.attach(environment);
    // 广播ApplicationEnvironmentPreparedEvent
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

- ApplicationEnvironmentPreparedEvent方法逻辑并不难，根据主类不同会有不同的子类实现。
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
				PropertySource<?> source = sources.get(name);
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



## Environment类

 ![image-20200107000721924](/home/chen/.config/Typora/typora-user-images/image-20200107000721924.png)