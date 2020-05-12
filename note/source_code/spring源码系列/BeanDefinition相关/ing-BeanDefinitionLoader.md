# BeanDefinitionLoader

> BeanDefinitionLoader是SpringApplication启动过程中对sources中包含的Bean对象加载的工具类。
>
> 他聚合了几种常用的BeanDefinition注册实现类，在其内部实现对不同配置源的加载。
>
> 无附加配置的情况下，仅加载主类到BeanFactory中。

<!-- more -->

---

[TOC]

## 概述

BeanDefinitionLoader的主要功能就是加载BeanDefinition。

下面是类的注解：

 ![image-20200501002314836](/home/chen/github/_java/pic/image-20200501002314836.png)

从sources中加载BeanDefinition，包括XML和JavaConfig类。

他聚合了以下的BeanDefinition加载类：

1. AnnotatedBeanDefinitionReader
2. XmlBeanDefinitionReader
3. ClassPathBeanDefinitionScanner

可以说是一个集合，提供一个总的入口。

**之前我以为此处会顺着配置源加载所有的BeanDefinition，但Debug发现，此处仅仅会将传入的配置源`sources`注册到BeanDefinitionRegistry中。**

具体的加载所有BeanDefinition的地方是ConfigurationClassPostProcessor类，详细可以看：

[ConfigurationClassPostProcessor](../BeanFactoryPostProcessor/ConfigurationClassPostProcessor.md)

我以SpringBoot Servlet Web为例子，无任何多余配置情况下，发现仅会将运行的主类的BeanDefinition注册。

在SpringBoot的启动流程中，该类在上下文准备阶段被调用，讲启动类的BeanDefinition注册进BeanFactory作为种子。





## 成员变量

类的成员变量如下：

 ![image-20200501002557051](/home/chen/github/_java/pic/image-20200501002557051.png)

可以看到BeanDefinitionLoader会持有上面三个类的引用，

猜测BeanDefinitionLoader就是通过判断不同的sources类型然后调用不同的加载类执行的。

`sources`中存放的就是需要遍历加载的源，其中包括主类。

`ResourceLoader`则是按资源加载器。





## 构造函数

构造函数如下：

 ![image-20200501002941182](/home/chen/github/_java/pic/image-20200501002941182.png)

配置sources属性，并初始化三个底层工具类

sources就是等等需要加载进来的BeanDefinition，类似下图：

 ![image-20200506225209802](/home/chen/github/_java/pic/image-20200506225209802.png)

最后还会在ClassPathBeanDefinitionScanner中记录已经加载过源。



## 上下文准备逻辑

```java
// SpringApplication#prepareContexts
load(context, sources.toArray(new Object[0]));
```

此行代码是整个加载过程的切入点，以当前上下文和sources作为入参。

详细的方法调用链可以看下面：

[SpringBoot启动过程中的上下文准备](./SpringBoot启动过程中的上下文准备.md)



## #load - 加载BeanDefinition

该方法就是具体的加载BeanDefinition的逻辑，之前在构造函数中已经初始化好了三个用于加载的的底层工具类。

工具都初始化好了，就可以开工了。

下面是BeanDefinitionLoader的方法列表：

 ![image-20200501233442059](/home/chen/github/_java/pic/image-20200501233442059.png)

看这一溜的load重载，你怕不怕？

以SpringBoot Servlet Web环境Debug发现，加载的总入口为load的无参方法。

load(Class),load(Resource),load(Package),load(CharSequence) 以上四个方法都是根据源类型不同，而采用的不同的加载模式。

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
```



## #load(Class)

该方法就是对Class类型的源的特殊处理。

```java
// BeanDefinitionLoader
private int load(Class<?> source) {
    	// Groovy的加载方式，此处忽略
        if (isGroovyPresent() && GroovyBeanDefinitionSource.class.isAssignableFrom(source)) {
                // Any GroovyLoaders added in beans{} DSL can contribute beans here
                GroovyBeanDefinitionSource loader = BeanUtils.instantiateClass(source, GroovyBeanDefinitionSource.class);
                load(loader);
        }
    	// 主类被@Component标示，就可以在此处被loader
        if (isComponent(source)) {
            	// 直接调用的annotatedReader,加载source的对象，并注册
            	// annotatedReader在构造函数中看到已经持有外层BeanDefinitionRegistry的引用对象了
                this.annotatedReader.register(source);
                return 1;
        }
        return 0;
}
```

此处可以看到忽略Groovy的部分，只有标注了@Component注解的类都是有效的源，都会在此时被解析并注册到BeanFactory中。

另外一路调用下来最终还是委托到AnnotatedBeanDefinitionReader执行具体的注册逻辑。

AnnotatedBeanDefinitionReader的逻辑可以看另外的文件：

[AnnotatedBeanDefinitionReader](./AnnotatedBeanDefinitionReader.md)




### 简单总结

该类主要负责将传入的Sources解析并注册到BeanDefinitionRegistry中。

但是解析和注册的流程都不在该类中，该类只是一个集成的类，根据资源的不同类型会调用不同的解析类去完成剩余的工作。





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

