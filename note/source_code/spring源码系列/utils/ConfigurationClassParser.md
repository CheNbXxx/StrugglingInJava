# ConfigurationClassParser

> 该类用于处理@Configuration类，并以读取到的类为源，递归解析所有配置类。

<!-- more -->

---

[TOC]



## 概述

ConfigurationClassParser的主要功能就是解析Configuration类。

在ConfigurationClassPostProcessor#processConfigBeanDefinitions中被创建并使用。

 ![image-20200506072509627](../../../../../pic/image-20200506072509627.png)

ConfigurationClassPostProcessor相关可以看:

[ConfigurationClassPostProcessor](../BeanFactoryPostProcessor/ConfigurationClassPostProcessor.md)



## 构造函数

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





## #parse

ConfigurationClassPostProcessor中就是以此方法作为切入。

入参集合中的都是候选的的满足基本要求的Configuration类。

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





## #processConfigurationClass 

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
            // 该类是否以Import形式加载
            if (configClass.isImported()) {
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
	// 递归处理完成之后，把configClass放入成员变量
    this.configurationClasses.put(configClass, configClass);
}
```

ConditionEvaluator类相关逻辑可以看：

[ConditionEvaluator](./ConditionEvaluator.md)





## #doProcessConfigurationClass

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

这里的细节有点多，应该要多分快分析了，按照注解不同来分。

该类主要就是处理入参中的configClass，提取其中的配置进行递归处理。

该类解析的注解如下：

1. @Component
2. @PropertySource  & @PropertySources
3. @ComponentScan
4. @Import
5. @Bean



## 解析方法

### #processMemberClasses - Component的内部类解析方法

```java
// ConfigurationClassParser
private void processMemberClasses(ConfigurationClass configClass, SourceClass sourceClass,
			Predicate<String> filter) throws IOException {
    	// 获取成员内部类，此方法会连同private的内部类一起获取
		Collection<SourceClass> memberClasses = sourceClass.getMemberClasses();
		if (!memberClasses.isEmpty()) {
                List<SourceClass> candidates = new ArrayList<>(memberClasses.size());
            	// 判断，该判断很常见，在进入processConfigurationClass处理之前都需要判断类是否符合
            	// ConfigurationCandidate的要求
                for (SourceClass memberClass : memberClasses) {
                        if (ConfigurationClassUtils.isConfigurationCandidate(memberClass.getMetadata()) &&
                                    !memberClass.getMetadata().getClassName().equals(configClass.getMetadata().getClassName())) {
                                candidates.add(memberClass);
                        }
                }
            	// 常规的Order排序
                OrderComparator.sort(candidates);
            	// 遍历获取到的内部类。
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

主要逻辑如下：

1. 获取所有的成员内部类
2. 筛选能够成为Configuration的内部类
3. processConfigurationClass处理

至于importStack的作用暂时未知。



### #retrieveBeanMethodMetadata - 类中所有的@Bean方法的解析

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



