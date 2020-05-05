# SpringBoot的工厂加载机制

> 工厂加载机制是`SpringBoot`的扩展点之一，首先`META-INF/spring.factories` 路径下配置相关子类，而且在框架运行中借由`SpringFactoriesLoader`加载到框架的上下文，实现自定义扩展。

<!-- more -->

---

[TOC]



## spring.factories文件

```Properties
# SpringApplicationRunListener就是基类，等号后面就是实现的需要加载的子类。
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
```

一个配置以等号划分key和value，以逗号划分多个value，斜杠换行。

要注意的是，value中的类名必须是key的子类，否则会报`IllegalArgumentException`。



## SpringFactoriesLoader

`SpringFactoriesLoader`就是Spring工厂加载机制的核心工具类。

`SpringFactoriesLoader`的源码并不复杂。

以下为其中的成员变量：

```java
 // 工厂加载机制的配置文件路径，写死不可修改
public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
// 日志
private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);
// 缓存，第一遍读取配置文件时，所有的k/v对都会放在cache中，不需要重复读文件。
// 以ClassLoader为key，value最终是LinkedMultiValueMap结构
private static final Map<ClassLoader, MultiValueMap<String, String>> cache = new ConcurrentReferenceHashMap<>();
```



在`SpringBoot`的应用启动过程中就使用了该类实现动态的加载，例如`SpringApplication`的构造函数中

```java
   // 这两行是SpringApplication构造函数中的两行代码	
   setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
	setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    
   // 可以看到最终调用还是使用的SpringFactoriesLoader.loadFactoryNames获取类的全限定名
  // 简单的可以看作，入参为一个类对象，获取配置文件中该类对象对应的所有实现的子类。
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
		return getSpringFactoriesInstances(type, new Class<?>[] {});
	}
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		ClassLoader classLoader = getClassLoader();
		// Use names and ensure unique to protect against duplicates
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}
```



### loadFactoryNames

- 该方法是获取全部的扩展类的全限定名。
- 只是读取配置文件中的信息，相关类的实例化流程需要自己实现。

```java
 
// 以一个Class类和ClassLoader为入参
public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader){
            String factoryTypeName = factoryType.getName();
            return loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
}

private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
           // 从缓存中获取，如果有直接退出。 
            MultiValueMap<String, String> result = cache.get(classLoader);
            if (result != null) {
                	return result;
            }
            try {
                    // 使用ClassLoader获取工厂配置资源的全路径
                    Enumeration<URL> urls = (classLoader != null ?
                            classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
                            ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
                    result = new LinkedMultiValueMap<>();
                    // 遍历获取到的spring.factories文件
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        UrlResource resource = new UrlResource(url);
                        // 获取其中的Properties属性值。
                        Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                        for (Map.Entry<?, ?> entry : properties.entrySet()) {
                                // 获取key值,去空
                                String factoryTypeName = ((String) entry.getKey()).trim();
                                // 按照逗号拆分value,并遍历添加到result
                                for (String factoryImplementationName : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
                                    	result.add(factoryTypeName, factoryImplementationName.trim());
                                }
                        }
                    }
                     // 添加到缓存	
                    cache.put(classLoader, result);
                    return result;
            }catch (IOException ex) {
                    throw new IllegalArgumentException("Unable to load factories from location [" +
                            FACTORIES_RESOURCE_LOCATION + "]", ex);
            }
	}
```

另外的`SpringFactoriesLoader`中也有提供默认的实例化方法，以及获取已经实例化的类的方法。



### loadFactories

- 和`loadFactoryNames`方法的区别就是该类返回的是实例化好的类

```java
public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
    	// 指定类加载器
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
		}
    	// 调用loadFactoryNames方法获取所有配置的类名
		List<String> factoryImplementationNames = loadFactoryNames(factoryType, classLoaderToUse);
		if (logger.isTraceEnabled()) {
			logger.trace("Loaded [" + factoryType.getName() + "] names: " + factoryImplementationNames);
		}
		List<T> result = new ArrayList<>(factoryImplementationNames.size());
    	// 调用SpringFactoriesLoader内部默认的实例化方法，遍历并实例化
		for (String factoryImplementationName : factoryImplementationNames) {
			result.add(instantiateFactory(factoryImplementationName, factoryType, classLoaderToUse));
		}
		// 按照Order排序
    	AnnotationAwareOrderComparator.sort(result);
		return result;
	}
```



### instantiateFactory

`SpringFactoriesLoader`中默认的实例化方法。

```java
// 实例化的核心方法，还是调用ClassUtils的forName方法，其他的都是异常处理
// ClassUtils.forName还包含各种数组的二维数组的实例化。
// 对于普通对象来说，就相当于Class.forName()方法
Class<?> factoryImplementationClass = ClassUtils.forName(factoryImplementationName, classLoader);

// 实际就相当于clazz.newInstance()
return (T) ReflectionUtils.accessibleConstructor(factoryImplementationClass).newInstance();
```



## SpringBoot启动流程中的工厂加载机制

- SpringBoot启动流程个中也会使用工厂加载机制加载一些类，但并不一定使用默认的构造函数，而是使用自定义的带参构造。



### SpringApplication

```java
// SpringApplication的构造函数中的两行初始化代码
setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));

// run方法里面获取监听器
private SpringApplicationRunListeners getRunListeners(String[] args) {
    Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
    return new SpringApplicationRunListeners(logger,
			getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));	}
```



### getSpringFactoriesInstance

```java
	// SpringApplication 	
     private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
         	 // 空壳方法，直接调用重载
			return getSpringFactoriesInstances(type, new Class<?>[] {});
	}
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
         // 获取当前的类加载器
		ClassLoader classLoader = getClassLoader();
         // 获取配置中的类全限定名，去重 
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
         // 不使用SpringFactoriesLoader的默认构造，调用自定义方法
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}
	// SpringApplication内部实现的实例化方法
	@SuppressWarnings("unchecked")
	private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
			ClassLoader classLoader, Object[] args, Set<String> names) {
		List<T> instances = new ArrayList<>(names.size());
		for (String name : names) {
			try {
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);
				Assert.isAssignable(type, instanceClass);
                 // 获取包含指定参数的构造函数
				Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
                 // 使用固定的构造函数和入参实例化对象
				T instance = (T) BeanUtils.instantiateClass(constructor, args);
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, ex);
			}
		}
		return instances;
	}
```



