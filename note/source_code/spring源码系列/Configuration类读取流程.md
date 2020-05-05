# Configuration类读取流程

>  本来只想简单看一下ConfigurationClassPostProcessor的，但是发现是个无底深坑，流程很复杂，细节也很多。

本文主要以上下文刷新阶段所调用的ConfigurationClassPostProcessor为切入点，分析整个JavaConfig类（用于配置@Conf）的加载过程。

包括一部分自动配置的部分（@Import）。



<!-- more -->

---

[TOC]



## ConfigurationClassPostProcessor

该类实现了BeanDefinitionRegistryPostProcessor，所以在上下文刷新阶段被调用。

负责加载所有的JavaConfig类，以及所有BeanDefinition。

以下即为ConfigurationClassPostProcessor的类签名：

 ![image-20200502230721431](/home/chen/github/_java/pic/image-20200502230721431.png)

图中可以看到，ConfigurationClassPostProcessor实现了BeanDefinitionRegistryPostProcessor接口，另外还有PriorityOrdered，以及一串的XXXXAware接口。

Aware接口会在创建该对象的时候注入进依赖，但是调用是发生在上下文刷新阶段，不知道这个注入如何实现。

BeanDefinitionRegistryPostProcessor接口子类会在刷新阶段统一被调用。

实现了PriorityOrdered则说明相比于实现了Ordered的以及普通的，该类具有较高的执行优先级。

**该类在SharedMetadataReaderFactoryContextInitializer#CachingMetadataReaderFactoryPostProcessor中通过BeanFactoryPostProcessor的形式注册到上下文中。**



另外需要注意的是：

 ![image-20200502232439693](/home/chen/github/_java/pic/image-20200502232439693.png)

根据PriorityOrdered的重载方法。

**该类具有最低优先级，所以任何实现了BeanDefinitionRegistryPostProcessor和PriorityOrdered接口的类都会在其之前被执行。**

这里的最低优先级是指在实现了PriorityOrdered的所有类。

BeanPostProcessor执行流程相关内容可以看下面：

[PostProcessorRegistrationDelegate](./PostProcessorRegistrationDelegate.md)



ConfigurationClassPostProcessor也实现了BeanFactoryPostProcessor的postProcessBeanFactory方法，但此处可能不会展开讲。



### #postProcessBeanDefinitionRegistry 

类的直接调用方法，上下文刷新时整体流程就是从这里切入的。

 ![image-20200505163350757](/home/chen/github/_java/pic/image-20200505163350757.png)

注释中就能看出，该方法的作用就是从Registry的Configuration类中往外衍生，获取更多的BeanDefinition。

所以以BeanDefinitionRegistry作为入参。

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

该方法中的逻辑就是在具体处理JavaConfig类前做一些验证。

获取当前Registry的HashCode，如果已经执行过则会在成员变量中保留，因此可以判断是否执行过。

processConfigBeanDefinitions方法看来就是主要的处理逻辑了。



#### #processConfigBeanDefinitions 

```java
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
        List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
    	// 获取名称
    	// 此处也可以看到，Configuration的来源是BeanDefinitionRegistry
        String[] candidateNames = registry.getBeanDefinitionNames()；
		// 遍历筛选标识了Configuration的类
        for (String beanName : candidateNames) {
                BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            	// 根据debug日志，猜测应该是配置文件是否已经加载的标志
                if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
                        if (logger.isDebugEnabled()) {
                            	logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
                        }
                // 检查是否符合Configuration类，可以看一下什么样的才是符合规范的Configuration类
                // 具体执行逻辑可以看ConfigurationClassUtils
                } else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
                    	configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
                }
        }

        // Return immediately if no @Configuration classes were found
    	// 若Configuration类为空，直接退出，好理解
        if (configCandidates.isEmpty()) {
                return;
        }

        // Sort by previously determined @Order value, if applicable
    	// 根据Order排序
    	// 在Check的时候就会把Order的值提取出来放到属性中，这次再次获取
        configCandidates.sort((bd1, bd2) -> {
                int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
                int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
                return Integer.compare(i1, i2);
        });

        // Detect any custom bean name generation strategy supplied through the enclosing application context
    	// 处理单例Bean，将BeanRegistry转化为单例形式
    	// 并设置本地BeanNameGenerator
        SingletonBeanRegistry sbr = null;
        if (registry instanceof SingletonBeanRegistry) {
                sbr = (SingletonBeanRegistry) registry;
                if (!this.localBeanNameGeneratorSet) {
                        BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
                            AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
                        if (generator != null) {
                            	// 两个BeanNameGenerator成员变量，具体作用未知。
                                this.componentScanBeanNameGenerator = generator;
                                this.importBeanNameGenerator = generator;
                        }
                }
        }
	
    	// 空的情况下创建环境容器
    	// 可能只是需要一个容器，并不会对环境有什么要求
        if (this.environment == null) {
            	this.environment = new StandardEnvironment();
        }

        // Parse each @Configuration class
    	// 创建Configuration类的解析器
    	// 此处可以看到全部参数都沿用了当前的
        ConfigurationClassParser parser = new ConfigurationClassParser(
            this.metadataReaderFactory, this.problemReporter, this.environment,
            this.resourceLoader, this.componentScanBeanNameGenerator, registry);

        Set<BeanDefinitionHolderbeanDef> candidates = new LinkedHashSet<>(configCandidates);
        Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
        do {
            	// 解析并验证筛选后剩余的
                parser.parse(candidates);
                parser.validate();
				// 从Parser的成员变量中获取
            	// 估计该成员变量就是解析之后存放ConfigurationClasses的
                Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
                configClasses.removeAll(alreadyParsed);

                // Read the model and create bean definitions based on its content
            	// 阅读模型并根据其BeanDefinition创建内容
                if (this.reader == null) {
                        this.reader = new ConfigurationClassBeanDefinitionReader(
                            registry, this.sourceExtractor, this.resourceLoader, this.environment,
                            this.importBeanNameGenerator, parser.getImportRegistry());
                }
            	// 读取BeanDefinition，
            	// ！！！这一步才是读取所有的BeanDefinition的主要流程
                this.reader.loadBeanDefinitions(configClasses);
                alreadyParsed.addAll(configClasses);

                candidates.clear();
            	// 判断BeanDefinition是否有多出来
                if (registry.getBeanDefinitionCount() > candidateNames.length) {
                        String[] newCandidateNames = registry.getBeanDefinitionNames();
                        Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
                        Set<String> alreadyParsedClasses = new HashSet<>();
                        for (ConfigurationClass configurationClass : alreadyParsed) {
                            	alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
                        }
                    	// 遍历多出来的BeanDefinition是否有符合条件的Configuration类
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
        } while (!candidates.isEmpty());

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

流程如下：

1. 从BeanDefinitionRegistry中获取所有的BeanDefinition
2. 筛选合法的ConfigurationClass并按照Order排序
3. 创建ConfigurationClassParser
4. 对经过筛选的BeanDefinition进行解析，验证
5. ConfigurationClassBeanDefinitionReader加载所有Configuration类中的BeanDefinition
6. 继续从Registry中获取新的BeanDefinition，然后的回到第四步循环，直到没有新增（这里就没有排序了）。
7. 注册特殊的Bean对象，清除缓存。

总结一下好像就是：

**筛选候选的Configuration类，创建对应的Parser类，循环解析直到Registry中没有产生新的符合Configuration条件的BeanDefinition。** 






## ConfigurationClassUtils

该类都是一些在加载ConfigurationClass的时候的相关工具类。

主要是检查判断用。



### #checkConfigurationClassCandidate

```java
// ConfigurationClassUtils
public static boolean checkConfigurationClassCandidate(
        BeanDefinition beanDef, MetadataReaderFactory metadataReaderFactory) {
    	// 获取类名称
        String className = beanDef.getBeanClassName();
    	// BeanDefinition对象中的className为空或者工厂方法不为空则退出。
        // 退出逻辑未知？？
        if (className == null || beanDef.getFactoryMethodName() != null) {
            	return false;
        }
	
    	// 转化为AnnotationMetadata
    	// 不同的BeanDefinition有不同的转化方式
        AnnotationMetadata metadata;
        if (beanDef instanceof AnnotatedBeanDefinition &&
                className.equals(((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
                // Can reuse the pre-parsed metadata from the given BeanDefinition...
                metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
        } else if (beanDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
                // Check already loaded Class if present...
                // since we possibly can't even load the class file for this Class.
               Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
               if (BeanFactoryPostProcessor.class.isAssignableFrom(beanClass) ||
                        BeanPostProcessor.class.isAssignableFrom(beanClass) ||
                        AopInfrastructureBean.class.isAssignableFrom(beanClass) ||
                        EventListenerFactory.class.isAssignableFrom(beanClass)) {
                        return false;
                    }
                    metadata = AnnotationMetadata.introspect(beanClass);
        } else {
                try {
                    	MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
                    	metadata = metadataReader.getAnnotationMetadata();
                } catch (IOException ex) {
                        if (logger.isDebugEnabled()) {
                            	logger.debug("Could not find class file for introspecting configuration annotations: " +
                                         	className, ex);
                        }
                        return false;
                }
        }
		
    	// AnnotationMetadata转化成功，之后的metadata可以直接用。
    	// 获取配置Map
        Map<String, Object> config = metadata.getAnnotationAttributes(Configuration.class.getName());
    	// 设置属性，这里可以看到
    	// CONFIGURATION_CLASS_ATTRIBUTE这个属性在检查的时候塞进去的
        if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
            	beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
        } else if (config != null || isConfigurationCandidate(metadata)) {
            	beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
        } else {
            	return false;
        }

        // It's a full or lite configuration candidate... Let's determine the order value, if any.
    	// 设置Order属性
        Integer order = getOrder(metadata);
        if (order != null) {
            	beanDef.setAttribute(ORDER_ATTRIBUTE, order);
        }

        return true;
}
```

显而易见的，该类主要是对beanDef的检查，**判断是否符合Configuration类的条件。**

能成为Configuration类的BeanDefinition的条件如下：

1. ClassName不为空
2. FactoryMethodName为空
3. 如果是AbstractBeanDefinition，则不能继承`BeanFactoryPostProcessor`，`BeanPostProcessor`，`AopInfrastructureBean`，`EventListenerFactory`中的任意一种



### #isConfigurationCandidate 

该类用于初步检查是否是Configuration类。

```java
// ConfigurationClassUtils
private static final Set<String> candidateIndicators = new HashSet<>(8);
static {
        candidateIndicators.add(Component.class.getName());
        candidateIndicators.add(ComponentScan.class.getName());
        candidateIndicators.add(Import.class.getName());
        candidateIndicators.add(ImportResource.class.getName());
}

public static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
        // 是接口就直接退出
        if (metadata.isInterface()) {
            	return false;
        }

        // Any of the typical annotations found?
    	// 标示了candidateIndicators中的任意一个注解，四个基本注解在上面
        for (String indicator : candidateIndicators) {
                if (metadata.isAnnotated(indicator)) {
                        return true;
                }
        }

        // Finally, let's look for @Bean methods...
    	// 检查带@Bean注释的方法
        try {
            	return metadata.hasAnnotatedMethods(Bean.class.getName());
        } catch (Throwable ex) {
                if (logger.isDebugEnabled()) {
                    	logger.debug("Failed to introspect @Bean methods on class [" + metadata.getClassName() + "]: " + ex);
                }
                return false;
        }
}
```

该类中可以看出成为Configuration类的候选条件：

1. 不能是接口
2. 标识了`@Component`，`@ComponentScan`，`@Import`，`@ImportResource`中的一个。
3. 类中有方法被@Bean标识

注意此处只是候选，是否可以解析还需要Check去判断。



## ConfigurationClassParser - Configuration类解析器

主要负责解析Configuration类



### 构造函数

在解析Configuration之前会先初始化整个解析器，用的如下构造函数。

```java
public ConfigurationClassParser(MetadataReaderFactory metadataReaderFactory,
                                ProblemReporter problemReporter, Environment environment, ResourceLoader resourceLoader,
                                BeanNameGenerator componentScanBeanNameGenerator, BeanDefinitionRegistry registry) {
        this.metadataReaderFactory = metadataReaderFactory;
        this.problemReporter = problemReporter;
        this.environment = environment;
        this.resourceLoader = resourceLoader;
        this.registry = registry;
        this.componentScanParser = new ComponentScanAnnotationParser(
            environment, resourceLoader, componentScanBeanNameGenerator, registry);
        this.conditionEvaluator = new ConditionEvaluator(registry, environment, resourceLoader);
}
```

可以看到其中包括了enviroment,register,resourceLoader等熟悉的组件。



### #parse - 解析入口

ConfigurationClassPostProcessor中就是以此方法作为切入。

入参就是在postProcessBeanDefinitionRegistry中**筛选并排序**的Configuration类的BeanDefinition，Holder只是一层包装。

也就是说入参集合中的都是候选的的满足基本要求的Configuration类。

```java
// ConfigurationClassParser
public void parse(Set<BeanDefinitionHolder> configCandidates) {
		// 遍历入参
    	for (BeanDefinitionHolder holder : configCandidates) {
                BeanDefinition bd = holder.getBeanDefinition();
                try {
                        // 这个判断的流程在ConfigurationClassUtils#checkConfigurationClassCandidate中也存在
                        // 根据BeanDefinition的实例类型调用重载的parse方法
                        if (bd instanceof AnnotatedBeanDefinition) {
                                // 传递的是BeanDefinition中的元数据，
                                parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
                        } else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
                                parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
                        } else {
                                parse(bd.getBeanClassName(), holder.getBeanName());
                        }
                }  catch (BeanDefinitionStoreException ex) {
                        throw ex;
                }  catch (Throwable ex) {
                    	throw new BeanDefinitionStoreException(
                        	"Failed to parse configuration class [" + bd.getBeanClassName() + "]", ex);
                }
        }
        this.deferredImportSelectorHandler.process();
}
```

如下就是AnnotatedBeanDefinition形式的BeanDefinition的解析方法：

```java
// Predicate就是一个简单的判断，输入一个参数，返回true or false
private static final Predicate<String> DEFAULT_EXCLUSION_FILTER = className ->
			(className.startsWith("java.lang.annotation.") || className.startsWith("org.springframework.stereotype."));

// ConfigurationClassParser
protected final void parse(AnnotationMetadata metadata, String beanName) throws IOException {
    	// 这里会对AnnotationMetadata做一个包装，以ConfigurationClass的形式传递到下个方法
		processConfigurationClass(new ConfigurationClass(metadata, beanName), DEFAULT_EXCLUSION_FILTER);
}
```





### processConfigurationClass - 主解析方法

```java
protected void processConfigurationClass(ConfigurationClass configClass, Predicate<String> filter) throws IOException {
   	// 判断是否需要跳过
    // 判断逻辑暂时忽略
    if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
        	return;
    }
	// 方法最下面会把已经处理过的类加入到configurationClasses中
    // 所以existingClass不为空就表示该类已经处理过了。
    ConfigurationClass existingClass = this.configurationClasses.get(configClass);
    if (existingClass != null) {
            if (configClass.isImported()) {
                	// @Import ??
                    if (existingClass.isImported()) {
                        	existingClass.mergeImportedBy(configClass);
                    }
                    // Otherwise ignore new imported config class; existing non-imported class overrides it.
                    return;
            } else {
                // Explicit bean definition found, probadoProcessConfigurationClassbly replacing an import.
                // Let's remove the old one and go with the new one.
                this.configurationClasses.remove(configClass);
                this.knownSuperclasses.values().removeIf(configClass::equals);
            }
    }

    // Recursively process the configuration class and its superclass hierarchy.
    // 对configClass做一个简单包装
    // SourceClass就是简单的包装类，类中也包含了一些解析的方法
    SourceClass sourceClass = asSourceClass(configClass, filter);
    do {
        sourceClass = doProcessConfigurationClass(configClass, sourceClass, filter);
    } while (sourceClass != null);
    // 并且获取的BeanDefinition都会放在configClass对象中
    // 算是做一个标记
    this.configurationClasses.put(configClass, configClass);
}
```

ConditionEvaluator类估计要单独看一下，在判断Condition的时候的整体逻辑。



### doProcessConfigurationClass - 内部解析方法

```java
@Nullable
	protected final SourceClass doProcessConfigurationClass(
			ConfigurationClass configClass, SourceClass sourceClass, Predicate<String> filter)
			throws IOException {
			// 处理Component注解的内部类
            if (configClass.getMetadata().isAnnotated(Component.class.getName())) {
                    // Recursively process any member (nested) classes first
                	// 递归处理成员内部类
                	processMemberClasses(configClass, sourceClass, filter);
            }

            // Process any @PropertySource annotation
        	// 处理PropertySource和PropertySources的方法
        	// PropertySources中包含了一个PropertySource的数组
        	// attributesForRepeatable就是组合这两个注解，最后返回一个PropertySource的注解数组
            for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
                    sourceClass.getMetadata(), PropertySources.class,
                    org.springframework.context.annotation.PropertySource.class)) {
                if (this.environment instanceof ConfigurableEnvironment) {
                        // 处理属性配置
                        processPropertySource(propertySource);
                } else {
                        logger.info("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
                                "]. Reason: Environment must implement ConfigurableEnvironment");
                }
            }

            // Process any @ComponentScan annotations
        	// 处理ComponentScangetMetadata
            Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
                    sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
            if (!componentScans.isEmpty() &&
                    !this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
                    for (AnnotationAttributes componentScan : componentScans) {
                        // The config class is annotated with @ComponentScan -> perform the scan immediately
                        Set<BeanDefinitionHolder> scannedBeanDefinitions =
                                this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
                        // Check the set of scanned definitions for any further config classes and parse recursively if needed
                        for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
                                BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
                                if (bdCand == null) {
                                        bdCand = holder.getBeanDefinition();
                                }
                                if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
                                        parse(bdCand.getBeanClassName(), holder.getBeanName());
                                }
                        }
                }
            }

            // Process any @Import annotations
            processImports(configClass, sourceClass, getImports(sourceClass), filter, true);

            // Process any @ImportResource annotations
            AnnotationAttributes importResource =
                    AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
            if (importResource != null) {
                String[] resources = importResource.getStringArray("locations");
                Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
                for (String resource : resources) {
                    String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
                    configClass.addImportedResource(resolvedResource, readerClass);
                }
            }

            // Process individual @Bean methods
        	// 检索类中的所有被@Bean修饰的
            Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
            for (MethodMetadata methodMetadata : beanMethods) {
                // 检索的结果是放在configClass中的
                configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
            }

            // Process default methods on interfaces
            processInterfaces(configClass, sourceClass);

            // Process superclass, if any
            if (sourceClass.getMetadata().hasSuperClass()) {
                    String superclass = sourceClass.getMetadata().getSuperClassName();
                    if (superclass != null && !superclass.startsWith("java") &&
                            !this.knownSuperclasses.containsKey(superclass)) {
                            this.knownSuperclasses.put(superclass, configClass);
                            // Superclass found, return its annotation metadata and recurseAnnotationConfigUtils.attributesForRepeatable(
                        sourceClass.getMetadata(), PropertySources.class,
                        org.springframework.context.annotation.PropertySource.class)
                            return sourceClass.getSuperClass();
                    }
            }

            // No superclass -> processing is complete
            return null;
	}
```

这里的细节有点多，应该要多分几个文件写了，按照注解不同来分。

该类主要就是处理入参中的configClass，提取其中的配置进行递归处理。

该类解析的注解如下：

1. @Component
2. @PropertySource  & @PropertySources
3. @ComponentScan
4. @Import
5. @Bean



### processMemberClasses - Component的内部类解析方法

```java
// ConfigurationClassParser
private void processMemberClasses(ConfigurationClass configClass, SourceClass sourceClass,
			Predicate<String> filter) throws IOException {
    	// 获取成员内部类此
		Collection<SourceClass> memberClasses = sourceClass.getMemberClasses();
		if (!memberClasses.isEmpty()) {
                List<SourceClass> candidates = new ArrayList<>(memberClasses.size());
            	// 是否可以称为候选
                for (SourceClass memberClass : memberClasses) {
                        if (ConfigurationClassUtils.isConfigurationCandidate(memberClass.getMetadata()) &&
                                    !memberClass.getMetadata().getClassName().equals(configClass.getMetadata().getClassName())) {
                                candidates.add(memberClass);
                        }
                }
            	// 常规的Order排序
                OrderComparator.sort(candidates);
            	// 遍历成员内部类
                for (SourceClass candidate : candidates) {
                        // importStack中已经包含则写入异常报告
                        if (this.importStack.contains(configClass)) {
                            	this.problemReporter.error(new CircularImportProblem(configClass, this.importStack));
                        } else {
                            // 推入importStack
                            this.importStack.push(configClass);
                            try {
                                	// 递归回到主解析方法进行处理
                                	processConfigurationClass(candidate.asConfigClass(configClass), filter);
                            } finally {
                                	// 处理完弹出
                                	this.importStack.pop();
                            }
                    }
                }
		}
	}
```

这个方法是检查@Component类中的成员内部类。

如果



### retrieveBeanMethodMetadata - 检索类中所有的@Bean方法

```java
	private Set<MethodMetadata> retrieveBeanMethodMetadata(SourceClass sourceClass) {
		AnnotationMetadata original = sourceClass.getMetadata();
		Set<MethodMetadata> beanMethods = original.getAnnotatedMethods(Bean.class.getName());
		if (beanMethods.size() > 1 && original instanceof StandardAnnotationMetadata) {
			// Try reading the class file via ASM for deterministic declaration order...
			// Unfortunately, the JVM's standard reflection returns methods in arbitrary
			// order, even between different runs of the same application on the same JVM.
			try {
				AnnotationMetadata asm =
						this.metadataReaderFactory.getMetadataReader(original.getClassName()).getAnnotationMetadata();
				Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Bean.class.getName());
				if (asmMethods.size() >= beanMethods.size()) {
					Set<MethodMetadata> selectedMethods = new LinkedHashSet<>(asmMethods.size());
					for (MethodMetadata asmMethod : asmMethods) {
						for (MethodMetadata beanMethod : beanMethods) {
							if (beanMethod.getMethodName().equals(asmMethod.getMethodName())) {
								selectedMethods.add(beanMethod);
								break;
							}
						}
					}
					if (selectedMethods.size() == beanMethods.size()) {
						// All reflection-detected methods found in ASM method set -> proceed
						beanMethods = selectedMethods;
					}
				}
			}
			catch (IOException ex) {
				logger.debug("Failed to read class file via ASM for determining @Bean method order", ex);
				// No worries, let's continue with the reflection metadata we started with...
			}
		}
		return beanMethods;
	}
```





## SourceClass

对source class的简单包装。

 ![image-20200504082326843](/home/chen/github/_java/pic/image-20200504082326843.png)

如类注释中所说，主要是为了以统一的方式处理这些Source Class，而忽略他们的加载过程。



### 成员变量

 ![image-20200504082501161](/home/chen/github/_java/pic/image-20200504082501161.png)

类中只有如上两个成员变量



### getMemberClasses - Configuration类中获取Bean

```java
public Collection<SourceClass> getMemberClasses() throws IOException {
    		// 复制一份配置源
			Object sourceToProcess = this.source;
    		// 从成员变量的注释中可以看到
    		// source就是Class或者MetadataReader
			if (sourceToProcess instanceof Class) {
                // 转化为类对象
				Class<?> sourceClass = (Class<?>) sourceToProcess;
				try {
                    	// 此处获取所有的内部类
                        Class<?>[] declaredClasses = sourceClass.getDeclaredClasses();
                        List<SourceClass> members = new ArrayList<>(declaredClasses.length);
                        for (Class<?> declaredClass : declaredClasses) {
                            // 遍历，包装并添加到members中
                            members.add(asSourceClass(declaredClass, DEFAULT_EXCLUSION_FILTER));
                        }
                    	// 返回
                        return members;
				} catch (NoClassDefFoundError err) {
					// getDeclaredClasses() failed because of non-resolvable dependencies
					// -> fall back to ASM below
					sourceToProcess = metadataReaderFactory.getMetadataReader(sourceClass.getName());
				}
			}

			// ASM-based resolution - safe for non-resolvable classes as well
			MetadataReader sourceReader = (MetadataReader) sourceToProcess;
			String[] memberClassNames = sourceReader.getClassMetadata().getMemberClassNames();
			List<SourceClass> members = new ArrayList<>(memberClassNames.length);
			for (String memberClassName : memberClassNames) {
				try {
					members.add(asSourceClass(memberClassName, DEFAULT_EXCLUSION_FILTER));
				}
				catch (IOException ex) {
					// Let's skip it if it's not resolvable - we're just looking for candidates
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to resolve member class [" + memberClassName +
								"] - not considering it as a configuration class candidate");
					}
				}
			}
			return members;
		}
```

**该方法会自行判断从反射和ASM两种方式返回source的内部类。**

在以反射获取内部类的时候，需要注意`sourceClass.getDeclaredClasses()`，该方法会获取自身的所有内部类，包括私有的，但是不包括父类的。

所以在Configuration中，私有的内部类也能被配置。