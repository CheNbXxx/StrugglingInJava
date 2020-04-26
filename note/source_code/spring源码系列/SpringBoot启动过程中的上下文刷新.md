# SpringBoot上下文刷新流程



- 刷新流程是SpringBoot中最为关键流程,方法主流程在`AbstractApplicationContext#refresh`中
- Bean的声明流程基本都在这个方法.

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
        // 准备刷新的一些必要条件.
        prepareRefresh();
		// 获取BeanFactory,其中可能会有刷新流程
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
        // BeanFactory的相关配置
        prepareBeanFactory(beanFactory);
        try {
            // 
            postProcessBeanFactory(beanFactory);
			// 调用所有的BeanFactoryPostProcessor
            invokeBeanFactoryPostProcessors(beanFactory);
			// 注册BeanPostProcessor
            registerBeanPostProcessors(beanFactory);
			// 初始化MessageSource
            initMessageSource();
			// 初始化广播器
            initApplicationEventMulticaster();
            // 刷新上下文
            onRefresh();
			// 检查监听器并注册
            registerListeners();
            // 
            finishBeanFactoryInitialization(beanFactory);
            // 
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



