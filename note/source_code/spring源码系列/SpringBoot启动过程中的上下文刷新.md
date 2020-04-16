# Spring的refresh方法





## 调用链

```java
// SpringApplication
private void refreshContext(ConfigurableApplicationContext context) {
        refresh(context);
        if (this.registerShutdownHook) {
                try {
                    	context.registerShutdownHook();
                }
                catch (AccessControlException ex) {
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





## AbstractApplicationContext#refresh

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



## prepareRefresh

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

        // 允许收集早起事件，刷新完成之后触发
        this.earlyApplicationEvents = new LinkedHashSet<>();
}
```



### validateRequiredProperties

验证必要属性是否存在

```java
// AbstractPropertyResolver
public void validateRequiredProperties() {
        MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
        for (String key : this.requiredProperties) {
                if (this.getProperty(key) == null) {
                    	ex.addMissingRequiredProperty(key);
                }
        }
        if (!ex.getMissingRequiredProperties().isEmpty()) {
            	throw ex;
        }
}
```



## obtainFreshBeanFactory

```java
// AbstractApplicationContext
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    refreshBeanFactory();
    return getBeanFactory();
}


// AbstractRefreshableApplicationContext
@Override
protected final void refreshBeanFactory() throws BeansException {
    // 如果BeanFactory存在,则先销毁
    if (hasBeanFactory()) {
        destroyBeans();
        closeBeanFactory();
    }
    try {
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        beanFactory.setSerializationId(getId());
        customizeBeanFactory(beanFactory);
        loadBeanDefinitions(beanFactory);
        synchronized (this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
        }
    }
    catch (IOException ex) {
        throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
    }
}
```



## prepareBeanFactory

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	// 设置类加载器
    beanFactory.setBeanClassLoader(getClassLoader());
    // 设置表达式解析器
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

    // Configure the bean factory with context callbacks.
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

    // BeanFactory interface not registered as resolvable type in a plain factory.
    // MessageSource registered (and found for autowiring) as a bean.
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

    // Register early post-processor for detecting inner beans as ApplicationListeners.
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

    // Detect a LoadTimeWeaver and prepare for weaving, if found.
    if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        // Set a temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }

    // Register default environment beans.
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



## invokeBeanFactoryPostProcessors - 处理BeanFactoryPostProcessors



```java
// AbstractApplicationContext
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    	// 委托给PostProcessorRegistrationDelegate执行调用操作
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

        // Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
        // (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
        if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
                beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
                beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }
}
```



### PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors

```java
// PostProcessorRegistrationDelegate
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
            	// 创建BeanDefinitionRegistry的引用对象
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
           		// 新建两个集合，按照实现接口不同划分
                List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
                // 存放实现了BeanDefinitionRegisterProcessor
                List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
			
            	// 遍历集合判断是否实现的是BeanDefinitionRegistryPostProcessor接口
                for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                        if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                                BeanDefinitionRegistryPostProcessor registryProcessor =
                                        (BeanDefinitionRegistryPostProcessor) postProcessor;
                                registryProcessor.postProcessBeanDefinitionRegistry(registry);
                                registryProcessors.add(registryProcessor);
                        }
                        else {
                            	regularPostProcessors.add(postProcessor);
                        }
                }

                // Do not initialize FactoryBeans here: We need to leave all regular beans
                // uninitialized to let the bean factory post-processors apply to them!
                // Separate between BeanDefinitionRegistryPostProcessors that implement
                // PriorityOrdered, Ordered, and the rest.
                List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

            	// 获取所有已注册的BeanDefinitionRegistryPostProcessor的Bean名称
                String[] postProcessorNames =
                        beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
            	// 遍历判断是否实现PriorityOrdered类
                for (String ppName : postProcessorNames) {
                    if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                        // 添加到currentRegistryProcessors和processedBeans集合中
                        currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                        processedBeans.add(ppName);
                    }
                }
            	// 排序
                sortPostProcessors(currentRegistryProcessors, beanFactory);
            	// 将整理出来的BeanDefinitionRegistryPostProcessor的Bean添加到registryProcessors集合
                registryProcessors.addAll(currentRegistryProcessors);
            	// 遍历调用postProcessBeanDefinitionRegistry方法
                invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
            	// 清空
                currentRegistryProcessors.clear();

            	// 逻辑类似
                postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
                for (String ppName : postProcessorNames) {
                    // 需要判断不存在与processedBeans中
                    /// 且实现了Ordered方法
                    if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
                        currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                        processedBeans.add(ppName);
                    }
                }
            	// 排序并执行
                sortPostProcessors(currentRegistryProcessors, beanFactory);
                registryProcessors.addAll(currentRegistryProcessors);
                invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
                currentRegistryProcessors.clear();

                boolean reiterate = true;
                while (reiterate) {
                    reiterate = false;
					// 再次获取全部BeanDefinitionRegistryPostProcessor的Bean对象
                    postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
                    for (String ppName : postProcessorNames) {
                        // 判断不能在processedBeans中，意味着没有实现PriorityOrdered和Ordered两个接口
                        if (!processedBeans.contains(ppName)) {
                            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                            processedBeans.add(ppName);
                            reiterate = true;
                        }
                    }
                    // 排序并执行
                    sortPostProcessors(currentRegistryProcessors, beanFactory);
                    registryProcessors.addAll(currentRegistryProcessors);
                    invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
                    currentRegistryProcessors.clear();
                }

            	// 执行所有的BeanFactoryPostProcessor#postProcessBeanFactory
                invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
                invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
            }

            else {
                // Invoke factory processors registered with the context instance.
                invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
            }

			// 获取所有的BeanFactoryPostProcessor，包含了上面的所有BeanDefinitionRegistryPostProcessor
            String[] postProcessorNames =
                    beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

            List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
            List<String> orderedPostProcessorNames = new ArrayList<>();
            List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    		// 根据实现类型划分到不同的集合
            for (String ppName : postProcessorNames) {
                	// 过滤掉已经执行过的BeanFactoryPostProcessor
                    if (processedBeans.contains(ppName)) {
                        	// skip - already processed in first phase above
                    } else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                        	// 此处的疑惑就是为什么PriorityOrdered的子类直接获取Bean对象
                        	priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
                    } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                        	orderedPostProcessorNames.add(ppName);
                    } else {
                        	nonOrderedPostProcessorNames.add(ppName);
                    }
            }
			
    		// 先执行继承了PriorityOrdered的
            sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
            invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

    		// 先执行继承了PriorityOrdered的,
            List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
    		// 遍历获取Bean对象
            for (String postProcessorName : orderedPostProcessorNames) {
                orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
            }
            sortPostProcessors(orderedPostProcessors, beanFactory);
            invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

    		// 再执行其他BeanFactoryPostProcessors
            List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
            for (String postProcessorName : nonOrderedPostProcessorNames) {
                nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
            }
            invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

            // Clear cached merged bean definitions since the post-processors might have
            // modified the original metadata, e.g. replacing placeholders in values...
            beanFactory.clearMetadataCache();
	}
```

BeanFactoryPostProcessors的执行顺序：

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



