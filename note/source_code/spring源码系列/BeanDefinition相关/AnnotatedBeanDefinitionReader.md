# AnnotatedBeanDefinitionReader

> 该类主要用来解析并注册以编程方式声明的Bean对象，例如启动主类。
>
> 本文以BeanDefinitionLoader为切入点详解doRegisterBean方法。



<!-- more -->

---

[TOC]

## 概述

 ![image-20200512151924315](/home/chen/github/_java/pic/image-20200512151924315.png)

上图即为该类的类注释，它提供了一个对编程方式注册的Bean类型的解析适配器。

用来替代ClassPathBeanDefinitionScanner类，处理方式和它一样但是明确了处理的类型。



## 内部方法

 ![image-20200512153425592](/home/chen/github/_java/pic/image-20200512153425592.png)

以上就是AnnotatedBeanDefinitionReader的方法列表。

可以看到其中包含大量的registerBean的重载函数，用于不同情形和要求下的Bean注册。

以下是两种重载方法和入口方法：

```java
// AnnotatedBeanDefinitionReader
public void register(Class<?>... componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            	registerBean(componentClass);
        }
}

// AnnotatedBeanDefinitionReader
public void registerBean(Class<?> beanClass) {
    	doRegisterBean(beanClass, null, null, null, null);
}

// AnnotatedBeanDefinitionReader
public <T> void registerBean(Class<T> beanClass, @Nullable String name, @Nullable Supplier<T> supplier) {
    doRegisterBean(beanClass, name, null, supplier, null);
}
```

可以看到最终都是调用的doRegisterBean。

另外类中注册的目标就是registry(BeanDefinitionRegistry)。





接下来就来看doRegisterBean方法。

## #doRegisterBean

**入参中除了beanClass之外都是可以为空的。**

```java
// AnnotatedBeanDefinitionReader
private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
                                @Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
                                @Nullable BeanDefinitionCustomizer[] customizers) {
    	// 将类包装成一个BeanDefinition
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
    	// 是否需要跳过，根据@Conditional注解，不满足则直接退出
        if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
            		return;
        }
		// 实例供应，debug时为空
    	// 在创建Bean的流程中，createBeanInstance方法还优先调用该实例供应方法。
        abd.setInstanceSupplier(supplier);
    	// 解析并填充Bean的生命周期
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
    
    	// 生成BeanName
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));
		
    	// 关于BeanDefinition的通用注解处理
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
    
    	// 设置Qualifier，其中包括Primary和Lazy，具体作用未知
        if (qualifiers != null) {
                for (Class<? extends Annotation> qualifier : qualifiers) {
                        if (Primary.class == qualifier) {
                            	abd.setPrimary(true);
                        } else if (Lazy.class == qualifier) {
                            	abd.setLazyInit(true);
                        } else {
                            	abd.addQualifier(new AutowireCandidateQualifier(qualifier));
                        }
                }
        }
    
    	// 设置注解自定义相关，作用未知
        if (customizers != null) {
            for (BeanDefinitionCustomizer customizer : customizers) {
                	customizer.customize(abd);
            }
        }
	
    	// BeanDefinition会进一步包装成BeanDefinitionHolder
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
    	// 域代理模式解析
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
    	// 注册BeanDefinition
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
}
```

该方法主要的作用就是往BeanDefinitionRegistry中注册BeanDefinition。

其中会对BeanClass做一系列的解析，包括生命周期和常见的注解。

如果BeanDefinition的生命周期不为NO，还会以外层RootBeanDefinition的形式多注册一次。





###  AnnotationScopeMetadataResolver#resolveScopeMetadata - 解析bean的生命周期

```java
// 生命周期主要看Scope注解
protected Class<? extends Annotation> scopeAnnotationType = Scope.class;
// AnnotationScopeMetadataResolver
public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) 
    	// 初始化默认的Scope,默认为单例模式，不使用代理
        ScopeMetadata metadata = new ScopeMetadata();
		// 如果不是AnnotatedBeanDefinition则直接返回默认的
        if (definition instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
            	// 从Bean的元数据中获取相关属性，
                AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(
                    annDef.getMetadata(), this.scopeAnnotationType);
                if (attributes != null) {
                    	// 从解析出来的元数据中获取信息，并填充到metadata中
                        metadata.setScopeName(attributes.getString("value"));
                        ScopedProxyMode proxyMode = attributes.getEnum("proxyMode");
                        if (proxyMode == ScopedProxyMode.DEFAULT) {
                            	proxyMode = this.defaultProxyMode;
                        }
                        metadata.setScopedProxyMode(proxyMode);
                }
        }
        return metadata;
}
```

以下为默认的ScopeMetadata属性：

 ![image-20200502124545370](/home/chen/github/_java/pic/image-20200502124545370.png)

该方法负责解析Bean的生命周期，**默认为单例模式，不使用代理。**

**且主要解析@Scope注解。**



### AnnotationConfigUtils#processCommonDefinitionAnnotations - 相关通用注解处理

```java
static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd, AnnotatedTypeMetadata metadata) {
    	// 读取@Lazy注解，并配置到AnnotatedBeanDefinition
        AnnotationAttributes lazy = attributesFor(metadata, Lazy.class);
        if (lazy != null) {
            	abd.setLazyInit(lazy.getBoolean("value"));
        } else if (abd.getMetadata() != metadata) {
                lazy = attributesFor(abd.getMetadata(), Lazy.class);
                if (lazy != null) {
                    	abd.setLazyInit(lazy.getBoolean("value"));
                }
        }
		// 获取@Primary注解
        if (metadata.isAnnotated(Primary.class.getName())) {
            	abd.setPrimary(true);
        }
    	// 读取@DependsOn注解属性
        AnnotationAttributes dependsOn = attributesFor(metadata, DependsOn.class);
        if (dependsOn != null) {
            	abd.setDependsOn(dependsOn.getStringArray("value"));
        }
    	// 读取@Role注解属性
        AnnotationAttributes role = attributesFor(metadata, Role.class);
        if (role != null) {
            	abd.setRole(role.getNumber("value").intValue());
        }
    	// 读取@Description注解属性
        AnnotationAttributes description = attributesFor(metadata, Description.class);
        if (description != null) {
            	abd.setDescription(description.getString("value"));
        }
}
```

**该方法就是从Bean的Class对象中提取各种注解属性，然后填充进AnnotatedBeanDefinition的成员变量**

提取的注解主要有：

1. @Lazy
2. @Primary
3. @DependsOn
4. @Role
5. @Description



### AnnotationConfigUtils.applyScopedProxyMode - 作用域相关处理

```java
// AnnotationConfigUtils
static BeanDefinitionHolder applyScopedProxyMode(
	ScopeMetadata metadata, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
	// 参数中的ScopeMetadata就是从BeanDefinition中解析出来的
    // 默认是单例
    ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
    // 这里就可以看到ScopedProxyMode的作用了
    // 如果为NO，此处就可以直接返回
    if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
        	return definition;
    }
    boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
    return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
}

// ScopedProxyCreator
// 该类就是简单的创建类，调用的方法最终会通过ScopedProxyUtils创建最终BeanDefinitionHolder
// 入参是BeanDefinitionHolder， 注册器，和是否使用CGLIB的标记
public static BeanDefinitionHolder createScopedProxy(
    BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry, boolean proxyTargetClass) {
    	return ScopedProxyUtils.createScopedProxy(definitionHolder, registry, proxyTargetClass);
}
```

以上的两个方法只是中间过渡，剪掉多余枝节，也就是ScopedProxyMode.NO的情况。

调用最终会到ScopedProxyUtils的方法中。



###  ScopedProxyUtils#createScopedProxy - 创建作用域的代理类

```java
public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,
                                                     BeanDefinitionRegistry registry, boolean proxyTargetClass) {
        String originalBeanName = definition.getBeanName();
    	// BeanDefinition未变，在代理中还是原来的
        BeanDefinition targetDefinition = definition.getBeanDefinition();
    	// 新的代理的Bean名称就是前缀（scopedTarget.）加上原来的Bean名称
        String targetBeanName = getTargetBeanName(originalBeanName);

        // Create a scoped proxy definition for the original bean name,
        // "hiding" the target bean in an internal target definition.
    	// 为SocpedProxyFactoryBean创建Bean对象
        RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);、
         // 设置内里的BeanDefinition，就是由新的Bean及其名称组成的BeanDefinitionHolder
         // 和原来比好像就是改了个名字 
        proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
    	// 设置原来的BeanDefinition，配置源和Role
        proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
        proxyDefinition.setSource(definition.getSource());
        proxyDefinition.setRole(targetDefinition.getRole());
		// 将名称填充到BeanDefinition的属性中
        proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
    	// proxyTargetClass是是否采用CGLIB的标记
        if (proxyTargetClass) {
                targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
                // ScopedProxyFactoryBean's "proxyTargetClass" default is TRUE, so we don't need to set it explicitly here.
        } else {
            	proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
        }

        // Copy autowire settings from original bean definition.
        proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
        proxyDefinition.setPrimary(targetDefinition.isPrimary());
        if (targetDefinition instanceof AbstractBeanDefinition) {
            	proxyDefinition.copyQualifiersFrom((AbstractBeanDefinition) targetDefinition);
        }

        // The target bean should be ignored in favor of the scoped proxy.
    	// 将原来的BeanDefinition属性置负
        targetDefinition.setAutowireCandidate(false);
        targetDefinition.setPrimary(false);

        // Register the target bean as separate bean in the factory.
    	// 将原来的BeanDefinition以现在的名称注册到BeanDefinitionRegistry
        registry.registerBeanDefinition(targetBeanName, targetDefinition);

        // Return the scoped proxy definition as primary bean definition
        // (potentially an inner bean).
        return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
}
```

此方法主要处理ScopedProxyMode不为NO的情况，方法中将原始的BeanDefinition包装成RootBeanDefinition。

并以前缀+原BeanDefinitionName的targetBeanName在Registry中注册原始的BeanDefinition。

返回的时候会以代理的BeanDefinition和原始的BeanName和代理的BeanDefinition返回Holder。



### BeanDefinitionReaderUtils#registerBeanDefinition - 注册BeanDefinition

```java
// BeanDefinitionReaderUtils
// 将BeanDefinitionHolder注册到BeanDefinitionRegistry中
public static void registerBeanDefinition(
        BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
        throws BeanDefinitionStoreException {
        // Register bean definition under primary name.
        String beanName = definitionHolder.getBeanName();
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

        // Register aliases for bean name, if any.
    	// BeanDefinition别名注册的逻辑暂时忽略
        String[] aliases = definitionHolder.getAliases();
        if (aliases != null) {
                for (String alias : aliases) {
                    	registry.registerAlias(beanName, alias);
                }
        }
}
```

**粗一看该方法很简单，就是将BeanDefinition注册到BeanDefinitionRegistry中，连同别名一起。**

但是联系上文发现，这次的beanName是原始的，BeanDefinition是新创建的RootBeanDefinition。

所以在Registry中，该BeanDefinition以不同的名称不同的形式注册了两次。

debug之后Registry中的内容也确实如我所想：

 ![image-20200504112754543](/home/chen/github/_java/pic/image-20200504112754543.png)

mvcApplication以不同的形式注册了两次