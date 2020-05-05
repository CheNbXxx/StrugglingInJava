# SpringBoot上下文刷新流程



- 刷新流程是SpringBoot中最为关键流程,方法主流程在`AbstractApplicationContext#refresh`中
- Bean的声明流程基本都在这个方法.
- 因为大部分的代码都蛮复杂的,应该会在别的文件里面单独分析,先走完整个流程.

---

<!-- more -->

[TOC]





## 外层调用链

```java
// SpringApplication
private void refreshContext(ConfigurableApplicationContext context) {
    	// 主要刷新逻辑
        refresh(context);
        if (this.registerShutdownHook) {
                try {
                    	// 调用失败的钩子方法
                    	context.registerShutdownHook();
                } catch (AccessControlException ex) {
                    // Not allowed in some environments.
                }
        }
}

// SpringApplication
protected void refresh(ApplicationContext applicationContext) {
    	// 断言判断是否继承
        Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
    	// 最终调用了AbstractApplicationContext的refresh方法
        ((AbstractApplicationContext) applicationContext).refresh();
}
```





## AbstractApplicationContext#refresh - 核心方法

```java
// AbstractApplicationContext
@Override
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
            // 准备刷新前的一些逻辑,记录启动时间,设置对应标识位等
            prepareRefresh();
            // 获取BeanFactory,其中可能会有刷新流程,Servlet Web环境不会刷新BeanFactory
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
            // BeanFactory的相关配置,设置类加载器,注册特殊的Bean
            prepareBeanFactory(beanFactory);
            try {
                	// 子类实现,前置方法,可根据不同的应用上下文
                    postProcessBeanFactory(beanFactory);
                    // 调用所有的BeanFactoryPostProcessor,在里面加载Configuration类
                    invokeBeanFactoryPostProcessors(beanFactory);
                    // 注册BeanPostProcessor,将BeanPostProcessor注册到应用上下文
                    registerBeanPostProcessors(beanFactory);
                    // 初始化消息源,国际化支持,暂不解析.
                    initMessageSource();
                    // 初始化广播器,结果就是ApplicationContext持有广播器的引用,且广播器在BeanFactory中也有Bean对象
                    initApplicationEventMulticaster();
                    // 刷新上下文,这也是延迟到子类实现的方法.
                    onRefresh();
                    // 检查监听器并注册,将BeanFactory以及早期的ApplicationListener注册到前一步的广播器中
                    registerListeners();
                    // 初始化所有的单例Bean
                    finishBeanFactoryInitialization(beanFactory);
                    // 应用上下文刷新之后的收尾.
                    finishRefresh();
            } catch (BeansException ex) {
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
            }finally {
                // Reset common introspection caches in Spring's core, since we
                // might not ever need metadata for singleton beans anymore...
                resetCommonCaches();
            }
        }
}
```



## prepareRefresh - 准备刷新

```java
protected void prepareRefresh() {
        // 记录启动时间
        this.startupDate = System.currentTimeMillis();
        // 设置对应标志位
        this.closed.set(false);
        this.active.set(true);
        if (logger.isDebugEnabled()) {
                if (logger.isTraceEnabled()) {
                    	logger.trace("Refreshing " + this);
                }else {
                    	logger.debug("Refreshing " + getDisplayName());
                }
        }
        // 初始化占位符资源
        initPropertySources();
        // 验证必要资源
        getEnvironment().validateRequiredProperties();
        // Store pre-refresh ApplicationListeners...
        // 创建前期的应用监听者集合，保存容器刷新钱的监听者
        if (this.earlyApplicationListeners == null) {
            	this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
        } else {
                this.applicationListeners.clear();
                this.applicationListeners.addAll(this.earlyApplicationListeners);
        }
        // 允许收集早期事件，刷新完成之后触发
        this.earlyApplicationEvents = new LinkedHashSet<>();
}
```

该方法主要的准备:

1. 设置启动时间和对应标志位
2. 初始化占位符的信息
3. 验证必要的资源
4. 处理上下文启动前期的监听者

这个前期的监听者是指在ApplicationContext在配置完成之前的事件都是由SpringApplicationRunListeners顶层分发的,之后则是以ApplicationContext为唯一事件分发器.



## obtainFreshBeanFactory - 获取BeanFactory

```java
// AbstractApplicationContext#obtainFreshBeanFactory
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    	// 看名字是刷新BeanFactory的意思
        refreshBeanFactory();
    	// 重新获取
        return getBeanFactory();
}

// GenericApplicationContext#refreshBeanFactory
@Override
protected final void refreshBeanFactory() throws IllegalStateException {
    if (!this.refreshed.compareAndSet(false, true)) {
        throw new IllegalStateException(
            "GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
    }
    this.beanFactory.setSerializationId(getId());
}

// GenericApplicationContext#getBeanFactory
@Override
public final ConfigurableListableBeanFactory getBeanFactory() {
    	return this.beanFactory;
}
```

- 在Servlet Web的应用里面,obtainFreshBeanFactory就是个获取的方法,并没有刷新流程.
- 具体的什么情况会刷新,什么情况只是简单获取.

## postProcessBeanFactory - BeanFactory的前置处理

不同的应用类型会有不同的实现,以下为常见的实现类:

 ![image-20200428223050944](/home/chen/github/_java/pic/image-20200428223050944.png)

以`AnnotationConfigServletWebServerApplicationContext`为上下文主类为例,作为切入了解.

```java
// AnnotationConfigServletWebServerApplicationContext	
@Override
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    	// 调用父类方法,也就是ServletWebServerApplicationContext的
        super.postProcessBeanFactory(beanFactory);
        if (this.basePackages != null && this.basePackages.length > 0) {
            	this.scanner.scan(this.basePackages);
        }
        if (!this.annotatedClasses.isEmpty()) {
            	this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
        }
}
// ServletWebServerApplicationContext
@Override
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    	// 新增一个BeanPostProcessor
        beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
    	// 注册一个忽略的类
        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
    	// 注册Web应用的生命周期
        registerWebApplicationScopes();
}
```





## prepareBeanFactory - 准备BeanFactory

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	// 设置类加载器
    beanFactory.setBeanClassLoader(getClassLoader());
    // 设置表达式解析器
    // 类似"#{...}" ,实际的类是SpelExpressionParser
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

    // 新增一个BeanPostProcessor
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    
    // 配置某些接口的忽略
    // 就是在BeanFactory中的ignoredDependencyInterfaces集合中添加一个类
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

	// 注册一些已经创建的Bean
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

    // 新增一个BeanPostProcessor
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

    // LoadTimeWeaver还不知道干啥的
    if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
            // Set a temporary ClassLoader for type matching.
            beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }

    // 注册环境Bean
    if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
        	beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
        	beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
        	beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
    }
}
```

该方法中主要注册的Bean依赖分别有:

1. BeanFactory
2. ResourceLoader
3. ApplicationEventPublisher
4. ApplicationContext
5. ConfigurableEnvironment,以及相关的系统属性

增加的BeanPostProcessor分别有:

1. ApplicationListenerDetector
2. ApplicationContextAwareProcessor

另外主要还是BeanFactory的一些配置

1. 设置类加载器
2. 配置表达式的解析器
3. 忽略的类集合



## invokeBeanFactoryPostProcessors - 处理BeanFactoryPostProcessors

```java
// AbstractApplicationContext
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    	// 委托给PostProcessorRegistrationDelegate执行调用操作
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

         // 这个方法在preparedBeanFactory也有相关的
        if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
                beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
                beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }
}
```

- [BeanFactoryPostProcessor相关的整理](./BeanFactoryPostProcessor.md)



## registerBeanPostProcessors - 注册BeanPostProcessors

```java
// AbstractApplicationContext
registerBeanPostProcessors(beanFactory);

protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}
```



### PostProcessorRegistrationDelegate#registerBeanPostProcessors

```java
public static void registerBeanPostProcessors(
    	ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
		// 获取所有的BeanPostProcessor类的Bean
    	String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

        // 添加BeanPostProcessorChecker
        int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
        beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// 按照实现的不同接口筛选，划分到不同的集合
        List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
        List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
        List<String> orderedPostProcessorNames = new ArrayList<>();
        List<String> nonOrderedPostProcessorNames = new ArrayList<>();
        for (String ppName : postProcessorNames) {
                if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                        // 按照惯例，实现了PriorityOrdered的直接获取Bean对象
                        BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                        priorityOrderedPostProcessors.add(pp);
                        if (pp instanceof MergedBeanDefinitionPostProcessor) {
                           		internalPostProcessors.add(pp);
                        }
                } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                    	orderedPostProcessorNames.add(ppName);
                } else {
                    	nonOrderedPostProcessorNames.add(ppName);
                }
        }
			
    	// 按照PriorityOrdered -> Ordered -> other
    	// 排序并添加到BeanFactory
        sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
        List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
        for (String ppName : orderedPostProcessorNames) {
                BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                orderedPostProcessors.add(pp);
                if (pp instanceof MergedBeanDefinitionPostProcessor) {
                    	internalPostProcessors.add(pp);
                }
        }
        sortPostProcessors(orderedPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, orderedPostProcessors);
        List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
        for (String ppName : nonOrderedPostProcessorNames) {
                BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                nonOrderedPostProcessors.add(pp);
                if (pp instanceof MergedBeanDefinitionPostProcessor) {
                    	internalPostProcessors.add(pp);
                }
        }
        registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

        // Finally, re-register all internal BeanPostProcessors.
        sortPostProcessors(internalPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, internalPostProcessors);

        // Re-register post-processor for detecting inner beans as ApplicationListeners,
        // moving it to the end of the processor chain (for picking up proxies etc).
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}
```





## initMessageSource - 初始化消息源

挖坑待填.





## initApplicationEventMulticaster - 初始化事件广播器

```java
public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

// AbstractApplicationContext
protected void initApplicationEventMulticaster() {
    	// 获取BeanFactory
    	// getBeanFactory就是通过不同子类各自实现了
       ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    	// 判断beanFactory中时都存在Bean对象
       if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
           		// 存在Bean对象就把对象取出来放在,并赋值给当前上下文
              this.applicationEventMulticaster =
                    beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
              if (logger.isTraceEnabled()) {
                 	logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
              }
       } else {
           	  // 如果没有广播器的情况下
           	  // 直接初始化一个SimpleApplicationEventMulticaster
              this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
           		// 注册单例的Bean
              beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
              if (logger.isTraceEnabled()) {
                	 logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
                       		"[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
              }
       }
}
```

上面代码展示的逻辑算是比较简单的了.

最终的结果就是确保`AbstractApplicationContext`应用的上下文,以及BeanFactory中都保留广播器实例.

如果BeanFactory已经有该实例对象了,可能通过BeanFactoryPostProcessor已经注册了,那么直接采用就好了.

不然就现场初始化一个.



## onRefresh - 刷新

在AbstractApplicationContext中,该方法完整信息如下:

```java
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}
```

也就是说该方法是由子类实现的,在此方法中可以注册特定的Bean或者其他扩展操作.

其中常用的子类有ServletWebServerApplicationContext,ReactiveWebServerApplicationContext等,根据应用的不同也会有不同的刷新逻辑.



##  registerListeners - 注册监听器

- 其实我有点不太明白为什么注册监听器要在onRefresh之后,在初始化完广播器不就可以注册了吗?

```java
// AbstractApplicationContext
protected void registerListeners() {
   // 将之前就在应用上下文的监听器注册到广播器中.
   for (ApplicationListener<?> listener : getApplicationListeners()) {
      	getApplicationEventMulticaster().addApplicationListener(listener);
   }

   // 获取所有ApplicationListener的Bean对象,并注册到广播器
    // 这里有注释提别提示了一个问题,千万不要在这里进行初始化,
    // 因为在此处直接初始化全部监听器的Bean对象,BeanPostProcesser就会对这些Bean对象失效.
   String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
   for (String listenerBeanName : listenerBeanNames) {
      	getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
   }

   // Publish early application events now that we finally have a multicaster...
    // 发布早期的事件
   Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
    // 置空,这个很重要!!  是一个标识
   this.earlyApplicationEvents = null;
   if (earlyEventsToProcess != null) {
       	  // 对事件进行统一的分发 
          for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
             	getApplicationEventMulticaster().multicastEvent(earlyEvent);
          }
   }
}
```

该方法主要就是将上下文中注册的监听器以及BeanFactory中的监听器都注册到广播器中,此后就是由广播器独家分发事件.

结合事件分发方法`publishEvent`,也马上能看出来earlyApplicationEvents的作用.

```java
// AbstractApplicationContext
...
if (this.earlyApplicationEvents != null) {
    	// earlyApplicationEvents不为空的情况下,并不会进行消息的广播
   		this.earlyApplicationEvents.add(applicationEvent);
} else {
   		getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
}
...
```

在earlyApplicationEvents不为null的情况下,事件分发全都被暂时拦截的,保存在earlyApplicationEvents中.

在将所有的ApplicationListener注册到广播器之后才会一口气分发下去.

所以如果以ApplicationContext作为监听者或者说观察者模式的实现方案时,要特别注意这点.

至于为什么需要在onRefresh之后在广播,可能是onRefresh之后,常规的Bean对象才都在BeanFactory中注册.有些可能并没有初始化.



## finishBeanFactoryInitialization - 初始化BeanFactory中的Bean对象

```java
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
   // 首先为BeanFactory初始化ConversionService
    // 此时他可能已经在BeanFactory中注册,但是并没有初始化,BeanFactory外层对象也并没有持有它的引用.
   if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
         beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
      		beanFactory.setConversionService(
            		beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
   }

   // Register a default embedded value resolver if no bean post-processor
   // (such as a PropertyPlaceholderConfigurer bean) registered any before:
   // at this point, primarily for resolution in annotation attribute values.
   if (!beanFactory.hasEmbeddedValueResolver()) {
      		beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
   }

   // LoadTimeWeaverAware这个类好像很常看到,暂时还不知道什么意思.
   String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
   for (String weaverAwareName : weaverAwareNames) {
      		getBean(weaverAwareName);
   }

   // 置空临时的类加载器
   beanFactory.setTempClassLoader(null);

   // 冻结所有的配置项,具体什么时候解冻未知.
   beanFactory.freezeConfiguration();

   // 预先实例化所有单例Bean
   beanFactory.preInstantiateSingletons();
```

该方法主要就是用来预先加载所有的单例Bean对象,在这之前还有一些检查和配置.

加载单例Bean之前还要先冻结配置项,避免加载的时候改来改去弄乱了.

下面是冻结配置项的操作,BeanFactory中用了两个单独的字段表示被冻结的配置项.

```java
// DefaultListableBeanFactory
@Override
public void freezeConfiguration() {
   this.configurationFrozen = true;
   this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
}
```

预先加载单例Bean应该是需要单独一个文件了.

留坑.



## finishRefresh - 收尾工作

```java
protected void finishRefresh() {
        // Clear context-level resource caches (such as ASM metadata from scanning).
    	// 清除容器级别的资源缓存
        clearResourceCaches();

        // Initialize lifecycle processor for this context.
    	// 初始化生命周期处理函数
        initLifecycleProcessor();

        // 刷新生命周期处理
        getLifecycleProcessor().onRefresh();

        // Publish the final event.
    	// 发布ContextRefreshedEvent事件
        publishEvent(new ContextRefreshedEvent(this));

        // MBean相关注册
        LiveBeansView.registerApplicationContext(this);
}
```



```java
// AbstractApplicationContext
// 初始化生命周期的处理器
protected void initLifecycleProcessor() {
    	// 这个处理过程好像和广播器的有点像
    	// 最终就是使ApplicationContext持有该处理器引用,并且BeanFactory中保留Bean对象
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
                this.lifecycleProcessor =
                    	beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
                if (logger.isTraceEnabled()) {
                    	logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
                }
        } else {
                DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
                defaultProcessor.setBeanFactory(beanFactory);
                this.lifecycleProcessor = defaultProcessor;
                beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
                if (logger.isTraceEnabled()) {
                    	logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
                                 "[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
                }
        }
}
```

这里不难发现,我们可以在此之前向BeanFactory中注册一个LifecycleProcessor的Bean对象,那么此处就会使用该对象.

如果没有注册,那就默认初始化为DefaultLifecycleProcessor.



其中蛮多的方法并没有讲清楚,先画圈在填色吧.