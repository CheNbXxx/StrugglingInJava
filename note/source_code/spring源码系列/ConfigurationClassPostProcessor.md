## ConfigurationClassPostProcessor

该类实现了BeanDefinitionRegistryPostProcessor，在上下文准备阶段被调用。

会加载并引用所有的JavaConfig类。



<!-- more -->

---

[TOC]

### ConfigurationClassPostProcessor 类信息

 ![image-20200502230721431](/home/chen/github/_java/pic/image-20200502230721431.png)

图中可以看到，ConfigurationClassPostProcessor实现了BeanDefinitionRegistryPostProcessor接口，另外还有PriorityOrdered，以及一串的XXXXAware接口。

Aware接口会在创建该对象的时候注入进依赖，但是调用是发生在上下文准备阶段，不知道这个注入如何实现。

BeanDefinitionRegistryPostProcessor接口子类会在准备阶段统一被调用。

实现了PriorityOrdered则说明相比于实现了Ordered的以及普通的，该类具有较高的执行优先级。

**该类在SharedMetadataReaderFactoryContextInitializer#CachingMetadataReaderFactoryPostProcessor中通过BeanFactoryPostProcessor的形式注册到上下文中。**



另外需要注意的是：

 ![image-20200502232439693](/home/chen/github/_java/pic/image-20200502232439693.png)

**该类具有最低优先级，所以任何实现了BeanDefinitionRegistryPostProcessor和PriorityOrdered接口的类都会在其之前被执行。**



BeanPostProcessor执行流程相关内容可以看下面：

[PostProcessorRegistrationDelegate](./PostProcessorRegistrationDelegate.md)



### 方法调用

```java
// ConfigurationClassPostProcessor
// Debug进来的第一个方法
@Override
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
    	// 获取BeanDefinitionRegistry
        int registryId = System.identityHashCode(registry);
    	// 检查是否有执行过该BeanPostProcessor
        if (this.registriesPostProcessed.contains(registryId)) {
                throw new IllegalStateException(
                    "postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
        }
        if (this.factoriesPostProcessed.contains(registryId)) {
                throw new IllegalStateException(
                    "postProcessBeanFactory already called on this post-processor against " + registry);
        }
    	// 添加进去，证明已经执行过
        this.registriesPostProcessed.add(registryId);
		
   		// 进一步调用
        processConfigBeanDefinitions(registry);
}
```



### processConfigBeanDefinitions

```java
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
        List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
    	// 获取名称
        String[] candidateNames = registry.getBeanDefinitionNames();
        for (String beanName : candidateNames) {
            	// 通过遍历名称来遍历BeanDefinition
                BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            	// 获取属性
                if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
                        if (logger.isDebugEnabled()) {
                            	logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
                        }
                } else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
                    	configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
                }
        }

        // Return immediately if no @Configuration classes were found
        if (configCandidates.isEmpty()) {
            return;
        }

        // Sort by previously determined @Order value, if applicable
        configCandidates.sort((bd1, bd2) -> {
            int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
            int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
            return Integer.compare(i1, i2);
        });

        // Detect any custom bean name generation strategy supplied through the enclosing application context
        SingletonBeanRegistry sbr = null;
        if (registry instanceof SingletonBeanRegistry) {
            sbr = (SingletonBeanRegistry) registry;
            if (!this.localBeanNameGeneratorSet) {
                BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
                    AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
                if (generator != null) {
                    this.componentScanBeanNameGenerator = generator;
                    this.importBeanNameGenerator = generator;
                }
            }
        }

        if (this.environment == null) {
            this.environment = new StandardEnvironment();
        }

        // Parse each @Configuration class
        ConfigurationClassParser parser = new ConfigurationClassParser(
            this.metadataReaderFactory, this.problemReporter, this.environment,
            this.resourceLoader, this.componentScanBeanNameGenerator, registry);

        Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
        Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
        do {
            parser.parse(candidates);
            parser.validate();

            Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
            configClasses.removeAll(alreadyParsed);

            // Read the model and create bean definitions based on its content
            if (this.reader == null) {
                this.reader = new ConfigurationClassBeanDefinitionReader(
                    registry, this.sourceExtractor, this.resourceLoader, this.environment,
                    this.importBeanNameGenerator, parser.getImportRegistry());
            }
            this.reader.loadBeanDefinitions(configClasses);
            alreadyParsed.addAll(configClasses);

            candidates.clear();
            if (registry.getBeanDefinitionCount() > candidateNames.length) {
                String[] newCandidateNames = registry.getBeanDefinitionNames();
                Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
                Set<String> alreadyParsedClasses = new HashSet<>();
                for (ConfigurationClass configurationClass : alreadyParsed) {
                    alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
                }
                for (String candidateName : newCandidateNames) {
                    if (!oldCandidateNames.contains(candidateName)) {
                        BeanDefinition bd = registry.getBeanDefinition(candidateName);
                        if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
                            !alreadyParsedClasses.contains(bd.getBeanClassName())) {
                            candidates.add(new BeanDefinitionHolder(bd, candidateName));
                        }
                    }
                }
                candidateNames = newCandidateNames;
            }
        }
        while (!candidates.isEmpty());

        // Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
        if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
            sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
        }

        if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
            // Clear cache in externally provided MetadataReaderFactory; this is a no-op
            // for a shared cache since it'll be cleared by the ApplicationContext.
            ((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
        }
}
```

