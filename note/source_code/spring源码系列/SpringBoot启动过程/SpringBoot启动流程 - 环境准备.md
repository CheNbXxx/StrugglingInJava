#  SpringBoot启动流程中的环境准备

> 该方法在启动过程中负责创建并配置具体的环境容器。

下面就是在run方法中的调用，可以看到入参有：

- applicationArguments - 包含了main方法中参数以及命令行参数.
- listeners -  一般情况下是EventPublishingRunListener，在上下文未初始化完毕之前，充当广播器。

```java
ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
```

单行代码也可以看出，该方法的主要作用就是**准备环境容器对象**，因为返回值就是ConfigurableEnvironment。



环境容器里面主要注意的就是Property和Profile两种配置。

**Property就是传统的k/v配置，Java是主语言的应该都不陌生。**

**Profiles简单来说就是对不同环境或者不同配置文件的切换配置，实际项目中通过修改该配置实现环境切换。**

<!-- more -->

---

[TOC]



## 环境容器概述 - Environment类族

上文说过方法的主要目的就是加载各种环境配置,并返回一个`ConfigurableEnvironment`.

因此首先简单了解一下`ConfigurableEnvironment`指的是什么.

SpringBoot Servlet Web环境下,方法对应的环境容器类是`StandardServletEnvironment`.

以下是`StandardServletEnvironment`的类图：

 ![image-20200329155324385](../../../../pic/image-20200329155324385.png)

StandardServletEnvironment继承关系中最上层的接口是`PropertyResolver`,它提供了key/value（Property）类型的属性访问方法。

以下是`PropertyResolver`的方法列表：

 ![image-20200414221834267](../../../../pic/image-20200414221834267.png)

`Environment`接口在其基础上扩展了对profiles的属性访问,保存了active的profiles。

profiles是Spring为了配置环境的动态切换而添加的参数。

最常见的使用就是生产环境和测试环境使用不同的配置文件，例如:`application-prod.yml`,`application-test.yml`

启动时就可以使用`spring.profiles.active`指定使用的配置文件。

以下为Environment的方法列表：

 ![image-20200414221802882](../../../../pic/image-20200414221802882.png)

`ConfigurablePropertyResolver`则另外扩展了类型转换的需求，以及自定义的占位符。

以下为其方法列表：

 ![image-20200414221902918](../../../../pic/image-20200414221902918.png)

可以看到有对ConversionService的Getter/Setter方法。

接下来的一些接口就是对以上的整合和扩展,具体不细说了。



以下是最终获取的`StandardServletEnvironment`的基本结构:

 ![image-20200329160858010](../../../../pic/image-20200329160858010.png)

propertySources是具体的属性类,每个类都标志的不同的读取位置。

PropertyResolver则是属性解析器,里面定义了前后缀等内容。

activeProfiles是激活的配置文件。

主要的还是PropertySource和Profiles两类配置。



## #prepareEnvironment - 主调用逻辑

```java
// SpringApplication	
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
                                                      ApplicationArguments applicationArguments) {
        // 获取或创建环境类，同样的的此处会根据不同的应用类型创建不同的环境容器类。
        ConfigurableEnvironment environment = getOrCreateEnvironment();
        // 配置环境
        configureEnvironment(environment, applicationArguments.getSourceArgs());
    	// 在将当前环境中的PropertySource保留一份自己的副本
        ConfigurationPropertySources.attach(environment);
        // 发布ApplicationEnvironmentPreparedEvent
        listeners.environmentPrepared(environment);
        // 将环境绑定到SpringApplication中
        bindToSpringApplication(environment);
    	// 如果要使用自定义的环境容器，此时则需要转换下对象
        if (!this.isCustomEnvironment) {
            	environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
                                                                                                   deduceEnvironmentClass());
        }
    	// 覆盖之前的副本，可能在中间环节会让PropertySource指向的引用改变
        ConfigurationPropertySources.attach(environment);
        return environment;
}
```



### #getOrCreateEnvironment - 获取或创建环境容器

根据不同的应用类型创建不同的环境类型。

SpringBoot Servlet Web环境使用`StandardservletEnvironment`类作为环境容器。

```java
private ConfigurableEnvironment getOrCreateEnvironment() {
    	// 如果已经有环境类的话就直接返回.
        if (this.environment != null) {
            return this.environment;
        }
        // 简单switch
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

该方法就是负责先创建出具体的容器类。

具体的构造函数就不展开了。



### #configureEnvironment  - 配置Profile和Property

```java
protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
   if (this.addConversionService) {
       // 单例模式，获取一个ConversionService对象
      ConversionService conversionService = ApplicationConversionService.getSharedInstance();
      environment.setConversionService((ConfigurableConversionService) conversionService);
   }
   configurePropertySources(environment, args);
   configureProfiles(environment, args);
}
```

**该方法主要配置Property的属性源，以及Profiles属性。**

另外有时还会配置一个ConversionService对象，ConversionService就是使用做属性转化的。



#### #configureProfiles - 配置Profile

```java
protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
        Set<String> profiles = new LinkedHashSet<>(this.additionalProfiles);
        profiles.addAll(Arrays.asList(environment.getActiveProfiles()));
        environment.setActiveProfiles(StringUtils.toStringArray(profiles));
}
```

- 简单结合当前应用启动类和环境类中的Profile配置。



#### #configurePropertySources - 配置Property

配置PropertySources中的PropertySources可以理解为就是AbstractEnvironment#propertySources。

```java
// CommandLinePropertySource
public static final String COMMAND_LINE_PROPERTY_SOURCE_NAME = "commandLineArgs";

// SpringApplication
protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
    	// 获取PropertySource
        MutablePropertySources sources = environment.getPropertySources();
        // 配置默认的属性，默认的属性可以通过别的方式塞进去，默认好像是没有默认属性
    	// 应该是在创建ApplicationContext的时候手动Set
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
                   } else {
                            sources.addFirst(new SimpleCommandLinePropertySource(args));
                   }
        }
}
```

该方法逻辑并不复杂：

1. 如果默认的配置属性存在，则添加到propertySources的最后
2. 配置命令行参数，不在新增，在就合并。

**PropertySource是放在末尾的，命令行参数是放在开头的，也不知道有啥区别。**



### ConfigurationPropertySources#attach - 内部备份

```java
private static final String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

// ConfigurationPropertySources
public static void attach(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
    	// 获取环境类的PropertySource，第二步也有配置这个
        MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
    	// 获取该属性的Value，我debug的Servlet Web环境下为空
        PropertySource<?> attached = sources.get(ATTACHED_PROPERTY_SOURCE_NAME)；
         //  不为空并且里面的数据和当前想要配置的数据不同就删了
        if (attached != null && attached.getSource() != sources) {
                sources.remove(ATTACHED_PROPERTY_SOURCE_NAME);
                attached = null;
        }
    	// 将自己的作为配置源信息的一部分塞回去
        if (attached == null) {
            	sources.addFirst(new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME,
                                                                            new SpringConfigurationPropertySources(sources)));
        }
}
```

**该方法主要是想要在PropertySources中保留一份自己的副本。**

具体的副本有啥用未知。

就算之前有但是如果和当前Source不同也会先删除，保证副本和当前环境的一致性。



### listeners#environmentPrepared - 触发ApplicationEnvironmentPreparedEvent事件

发布ApplicationEnvironmentPreparedEvent。

**ApplicationEnvironmentPreparedEvent在监听器中会加载yml和properties文件中的配置。**

此处会触发包含`ConfigFileApplicationListener`在内的七个监听器。

 ![image-20200329162414503](../../../../pic/image-20200329162414503.png)

加载yml和properties的详细过程可以看：

[ConfigFileApplicationListener](./ConfigFileApplicationListener.md).



### #bindToSpringApplication - 应用绑定

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

具体绑定流程待展开。



### ConfigurationPropertySources#attach - 内部重新备份

这是第二次调用该方法。

猜测是因为在绑定环境或者中间其他步骤中会直接替换整个的PropertySource的引用，所以此时要重新填充自举的属性。



## 总结

`prepareEnvironment`方法的主要作用就是加载配置，通过ConfigFileApplicationListener获取所有配置属性。

如开头所说，配置中最主要的两块内容是`Property`和`Profiles`。

Property除了加载别的属性之后，一他会存放一份副本在他的集合中，二还有一些默认配置和命令行配置。

总体的流程如下：

1. 创建环境类实例
2. 添加默认参数，命令行参数等一些配置到实例中，并配置Profile
3. 触发`ApplicationEnvironmentPreparedEvent`，读取配置文件到环境中
4. 将创建好的环境对象与当前的SpringApplication对象绑定

经过该流程，会返回一个统一的完整的Environment容器给SpringApplication。