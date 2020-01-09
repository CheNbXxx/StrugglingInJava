# SpringBoot的工厂加载机制



- 工厂加载机制是SpringBoot的扩展点之一，首先`META-INF/spring.factories` 路径下配置相关子类，而且在框架运行中借由`SpringFactoriesLoader`加载到框架的上下文，实现自定义扩展。



---



## spring.factories文件

```Properties
# SpringApplicationRunListener就是基类，等号后面就是实现的需要加载的子类。
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
```

一个配置以等号划分key和value，以逗号划分多个value，斜杠换行。





## SpringFactoriesLoader类

SpringFactoriesLoader就是Spring工厂加载机制的核心工具类。

以下为其中的成员变量：

```java
 // 工厂加载机制的配置文件路径    
public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
// 日志
	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);
// 缓存，第一遍读取配置文件时，所有的k/v对都会放在cache中，不需要重复读文件。
// 以ClassLoader为key，value最终是LinkedMultiValueMap结构
	private static final Map<ClassLoader, MultiValueMap<String, String>> cache = new ConcurrentReferenceHashMap<>();
```



在SpringBoot的应用启动过程中就使用了该类实现动态的加载，例如SpringApplication的构造函数中

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

- 该方法是获取全部的扩展类的全限定名

```java
 
// 以一个Class类和ClassLoader为入参
	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
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
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
	}
```



