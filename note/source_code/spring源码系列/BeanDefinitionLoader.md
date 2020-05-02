# BeanDefinitionLoader

- BeanDefinitionLoader主要负责对BeanDefinition的加载工作。
- 在SpringBoot的启动流程中，它在上下文准备阶段被调用，在上下文准备完成事件和上下文加载完成时间之间。
- 独立成一个文件记录之后可以像集合容器的源码一样慢慢分析这个类。



<!-- more -->

---

[TOC]

## 概述

BeanDefinitionLoader的主要功能就是加载BeanDefinition的。

下面是类的注解：

 ![image-20200501002314836](/home/chen/github/_java/pic/image-20200501002314836.png)

BeanDefinitionLoader会从底层的源中加载BeanDefinition，包括XML和JavaConfig类。

是AnnotatedBeanDefinitionReader，XmlBeanDefinitionReader，ClassPathBeanDefinitionScanner三个类的门面。

或者说是整合的工具类，具体的功能还是要靠上面三个类实现。

**之前我以为此处会顺着配置源加载所有的BeanDefinition，但Debug发现，此处仅仅会将传入的配置源注册到BeanDefinitionRegistry中。**

我以为SpringBoot Servlet Web为例子，Debug发现仅会将运行的主类的BeanDefinition注册。

具体的加载所有BeanDefinition的地方是ConfigurationClassPostProcessor类，详细可以看下文：





## 成员变量

类的成员变量如下：

 ![image-20200501002557051](/home/chen/github/_java/pic/image-20200501002557051.png)

可以看到BeanDefinitionLoader会持有上面三个类的引用，

猜测BeanDefinitionLoader就是通过判断不同的sources类型然后调用不同的加载类执行的。

`sources`中存放的就是需要遍历的源。

`ResourceLoader`则是源加载器。



## 构造函数

构造函数如下：

![image-20200501233621295](/home/chen/github/_java/pic/image-20200501233621295.png) ![image-20200501002941182](/home/chen/github/_java/pic/image-20200501002941182.png)

初始化三个底层类，配置sources。

最后会在ClassPathBeanDefinitionScanner中记录已经加载过源。



## Load - 加载BeanDefinition

具体的加载BeanDefinition的逻辑，之前的构造函数中已经初始化好了三个用于加载的的底层类以及一些工具类。

工具都初始化好了，就可以开工了。

下面是BeanDefinitionLoader的方法列表：

 ![image-20200501233442059](/home/chen/github/_java/pic/image-20200501233442059.png)

看这一溜的load重载，你怕不怕？

上面的方法中可知，加载的总入口为load的无参方法。

load(Class),load(Resource),load(Package),load(CharSequence)

以上四个方法都是根据源类型不同，而采用的不同的加载模式。

```java
// BeanDefinitionLoader
int load() {
       int count = 0;
    	// 遍历全部的资源，调用重载函数分派执行。
       for (Object source : this.sources) {
          	count += load(source);
       }
       return count;
}

// BeanDefinitionLoader
private int load(Object source) {
        Assert.notNull(source, "Source must not be null");
    	// 根据不同的资源类型调用不同的重载方法。
    	// Servlet Web环境下，没有别的配置
    	// source只有启动的主类一个
    	// 也就是进load((Class<?>) source)方法
    	if (source instanceof Class<?>) {
            	return load((Class<?>) source);
        }
        if (source instanceof Resource) {
            	return load((Resource) source);
        }
        if (source instanceof Package) {
            	return load((Package) source);
        }
        if (source instanceof CharSequence) {
            	return load((CharSequence) source);
        }
        throw new IllegalArgumentException("Invalid source type " + source.getClass());
}

// BeanDefinitionLoader
private int load(Class<?> source) {
        if (isGroovyPresent() && GroovyBeanDefinitionSource.class.isAssignableFrom(source)) {
                // Any GroovyLoaders added in beans{} DSL can contribute beans here
                GroovyBeanDefinitionSource loader = BeanUtils.instantiateClass(source, GroovyBeanDefinitionSource.class);
                load(loader);
        }
        if (isComponent(source)) {
            	// 直接调用的annotatedReader
                this.annotatedReader.register(source);
                return 1;
        }
        return 0;
}
```

一路调用下来最终是委托到AnnotatedBeanDefinitionReader执行具体的注册逻辑。



### AnnotatedBeanDefinitionReader  - register

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
    	// 域代理模式
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
    	// 注册BeanDefinition
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
}
```

该方法主要的作用就是往BeanDefinitionRegistry中注册BeanDefinition。

1. BeanDefinition会被进一步包装为BeanDefinitionHolder，然后进行注册。
2. 过程中的各种注解属性都会被包装到BeanDefinition中





####  AnnotationScopeMetadataResolver#resolveScopeMetadata - 解析bean的生命周期

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

该方法负责解析Bean的生命周期，默认为单例模式，不使用代理。

**且主要解析@Scope注解。**





#### processCommonDefinitionAnnotations - 相关通用注解处理

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

**该方法就是从Bean的Class对象中提取各种注解属性，然后填充进AnnotatedBeanDefinition**

提取的注解主要有：

1. @Lazy
2. @Primary
3. @DependsOn
4. @Role
5. @Description



#### AnnotationConfigUtils.applyScopedProxyMode - 作用域相关处理

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



####  ScopedProxyUtils#createScopedProxy - 创建作用域的代理类

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
        registry.registerBeanDefinition(targetBeanName, targetDefinition);

        // Return the scoped proxy definition as primary bean definition
        // (potentially an inner bean).
        return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
}
```

该方法会将原有的BeanDefinition替换名字后重新包装成RootBeanDefinition，并再次封装到BeanDefinitionHolder中。

此处可见BeanDefinitionHolder才是Bean最外层的持有类。

至于为什么要将BeanDefinition转化为RootBeanDefinition未知。



#### BeanDefinitionReaderUtils#registerBeanDefinition - 注册BeanDefinition

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
        String[] aliases = definitionHolder.getAliases();
        if (aliases != null) {
                for (String alias : aliases) {
                    	registry.registerAlias(beanName, alias);
                }
        }
}
```

该方法简单，就是将BeanDefinition注册到BeanDefinitionRegistry中，连同别名一起。



## ScopedProxyMode - 作用域代理模式

```java
public enum ScopedProxyMode {

   /**
    * Default typically equals {@link #NO}, unless a different default
    * has been configured at the component-scan instruction level.
    */
   DEFAULT,

   /**
    * Do not create a scoped proxy.
    * <p>This proxy-mode is not typically useful when used with a
    * non-singleton scoped instance, which should favor the use of the
    * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
    * is to be used as a dependency.
    */
   NO,

   /**
    * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
    * the class of the target object.
    */
   INTERFACES,

   /**
    * Create a class-based proxy (uses CGLIB).
    */
   TARGET_CLASS

}
```

