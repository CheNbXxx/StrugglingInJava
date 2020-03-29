## ApplicationEnvironmentPreparedEvent

- 妈的 太多Java8的Lambda表达式了看起来头好痛



在SpringBoot启动初期准备环境时发布的事件.

```java
	private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        // 工厂模式获取所有的EnvironmentPostProcessor
		List<EnvironmentPostProcessor> postProcessors = loadPostProcessors();
        // ConfigFileApplicationListener也作为一个EnvironmentPostProcessor加入调用链
		postProcessors.add(this);
        // 按照Order排序
		AnnotationAwareOrderComparator.sort(postProcessors);
		for (EnvironmentPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessEnvironment(event.getEnvironment(), event.getSpringApplication());
		}
	}
```



Debug时自带的`EnvironmentPostProcessor`

 ![image-20200329203541928](/home/chen/github/StrugglingInJava/pic/image-20200329203541928.png)

SystemEnvironmentPropertySourceEnvironmentPostProcessor是为了包装原有的系统属性.

其他的就先忽略.



#### ConfigFileApplicationListener#postProcessEnvironment

主要还是ConfigFileApplicationListener的postProcessEnvironment方法.

```java
//  ConfigFileApplicationListener
@Override
public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    addPropertySources(environment, application.getResourceLoader());
}

protected void addPropertySources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
       // 此处添加了一个随机数 ,作用未知
		RandomValuePropertySource.addToEnvironment(environment);
    	// 这里应该就是加载配置文件的过程了 
		new Loader(environment, resourceLoader).load();
	}
```

主要作用:

1. 添加一个随机数到配置中`environment.propertySource`
2. 加载本地配置文件.
3. 

#### 随机数的作用

添加的随机数如下:

 ![image-20200329205301295](/home/chen/github/StrugglingInJava/pic/image-20200329205301295.png)

 ![image-20200329205230168](/home/chen/github/StrugglingInJava/pic/image-20200329205230168.png)

上面就是RandomValuePropertySource的类注释.

大意应该是超越random的属性都是非法的.



#### 加载配置文件

```java
// ConfigFileApplicationListener
new Loader(environment, resourceLoader).load();

// ConfigFileApplicationListener@Loader
Loader(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
    // 配置了环境,占位符解析器,资源加载器,还有propertySourceLoader
    this.environment = environment;
    this.placeholdersResolver = new PropertySourcesPlaceholdersResolver(this.environment);
    this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
    this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class,
                                                                     getClass().getClassLoader());
}
```

获取到的两个PropertySourceLoader

 ![image-20200329205729718](/home/chen/github/StrugglingInJava/pic/image-20200329205729718.png)



以下就是整个配置文件加载过程的方法调用链,有点长而且好多load方法.

#### load方法

```java
// ConfigFileApplicationListener
private static final String DEFAULT_PROPERTIES = "defaultProperties";
private static final Set<String> LOAD_FILTERED_PROPERTY;
static {
    Set<String> filteredProperties = new HashSet<>();
    filteredProperties.add("spring.profiles.active");
    filteredProperties.add("spring.profiles.include");
    LOAD_FILTERED_PROPERTY = Collections.unmodifiableSet(filteredProperties);
}

// ConfigFileApplicationListener#load
void load() {
    FilteredPropertySource.apply(this.environment, DEFAULT_PROPERTIES, LOAD_FILTERED_PROPERTY,
                                 (defaultProperties) -> {
                                     this.profiles = new LinkedList<>();
                                     this.processedProfiles = new LinkedList<>();
                                     this.activatedProfiles = false;
                                     this.loaded = new LinkedHashMap<>();
                                     // 初始化profiles
                                     initializeProfiles();
                                     while (!this.profiles.isEmpty()) {
                                         Profile profile = this.profiles.poll();
                                         if (isDefaultProfile(profile)) {
                                             addProfileToEnvironment(profile.getName());
                                         }
                                         load(profile, this::getPositiveProfileFilter,
                                              addToLoaded(MutablePropertySources::addLast, false));
                                         this.processedProfiles.add(profile);
                                     }
                                     load(null, this::getNegativeProfileFilter, addToLoaded(MutablePropertySources::addFirst, true));
                                     addLoadedPropertySources();
                                     applyActiveProfiles(defaultProperties);
                                 });
}


// FilteredPropertySource#apply
// propertySourceName  ==>  defaultProperties
// filteredProperties  ==>  spring.profiles.active ＆spring.profiles.include
static void apply(ConfigurableEnvironment environment, String propertySourceName, Set<String> filteredProperties,
			Consumer<PropertySource<?>> operation) {
		MutablePropertySources propertySources = environment.getPropertySources();
		PropertySource<?> original = propertySources.get(propertySourceName);
		if (original == null) {
            // accept回跳到上面的内部类
			operation.accept(null);
			return;
		}
		propertySources.replace(propertySourceName, new FilteredPropertySource(original, filteredProperties));
		try {
			operation.accept(original);
		}
		finally {
			propertySources.replace(propertySourceName, original);
		}
	}
```



#### ConfigFileApplicationListener#initializeProfiles

```java

 public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";
public static final String INCLUDE_PROFILES_PROPERTY = "spring.profiles.include";

// ConfigFileApplicationListener#initializeProfiles
private void initializeProfiles() {
    // The default profile for these purposes is represented as null. We add it
    // first so that it is processed first and has lowest priority.
    this.profiles.add(null);
    Set<Profile> activatedViaProperty = getProfilesFromPxmlroperty(ACTIVE_PROFILES_PROPERTY);
    Set<Profile> includedViaProperty = getProfilesFromProperty(INCLUDE_PROFILES_PROPERTY);
    List<Profile> otherActiveProfiles = getOtherActiveProfiles(activatedViaProperty, includedViaProperty);
    this.profiles.addAll(otherActiveProfiles);
    // Any pre-existing active profiles set via property sources (e.g.
    // System properties) take precedence over those added in config files.
    this.profiles.addAll(includedViaProperty);
    addActiveProfiles(activatedViaProperty);
    if (this.profiles.size() == 1) { // only has null profile
        for (String defaultProfileName : this.environment.getDefaultProfiles()) {
            Profile defaultProfile = new Profile(defaultProfileName, true);
            this.profiles.add(defaultProfile);
        }
    }
}

// ConfigFileApplicationListener#getProfilesFromProperty
private Set<Profile> getProfilesFromProperty(String profilesProperty) {
    if (!this.environment.containsProperty(profilesProperty)) {
        return Collections.emptySet();
    }
    Binder binder = Binder.get(this.environment);
    Set<Profile> profiles = getProfiles(binder, profilesProperty);
    return new LinkedHashSet<>(profiles);
}
```



#### ConfigFileApplicationListener#load

```java
// ConfigFileApplicationListener#load
private void load(Profile profile, DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
    // 获取地址并遍历执行
    getSearchLocations().forEach((location) -> {
        boolean isFolder = location.endsWith("/");
        Set<String> names = isFolder ? getSearchNames() : NO_SEARCH_NAMES;
        names.forEach((name) -> load(location, name, profile, filterFactory, consumer));
    });
}

// 可配置的配置文件地址,如果配置该属性,默认的文件地址失效
public static final String CONFIG_LOCATION_PROPERTY = "spring.config.location";
// 附加的配置文件地址
public static final String CONFIG_ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location";
// 默认的配置文件地址
private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/";

// ConfigFileApplicationListener#getSearchLocations
// 该方法用于获取需要搜索的本地配置文件地址
// 默认就是上面的四个文件地址,逗号分割
private Set<String> getSearchLocations() {
    // 如果指定了spring.config.location,那么后面的就失效了
    if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
        return getSearchLocations(CONFIG_LOCATION_PROPERTY);
    }
    // 本地的附加配置文件地址
    Set<String> locations = getSearchLocations(CONFIG_ADDITIONAL_LOCATION_PROPERTY);
    locations.addAll(
        asResolvedSet(ConfigFileApplicationListener.this.searchLocations, DEFAULT_SEARCH_LOCATIONS));
    return locations;
}

// ConfigFileApplicationListener#asResolvedSet
// 分割并返回,对配置文件地址和名称都会进行分割
private Set<String> asResolvedSet(String value, String fallback) {
    List<String> list = Arrays.asList(StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(
        (value != null) ? this.environment.resolvePlaceholders(value) : fallback)));
    // 这个翻转一下什么意思
    Collections.reverse(list);
    // 去重返回
    return new LinkedHashSet<>(list);
}

// 默认的配置文件名称
private static final String DEFAULT_NAMES = "application";
// 配置项 - 配置文件名称,如果配置该属性,默认的文件名称失效
// 应该是可以以","分割
public static final String CONFIG_NAME_PROPERTY = "spring.config.name";

// ConfigFileApplicationListener#getSearchNames
// 获取配置文件的名称
private Set<String> getSearchNames() {
    if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
        String property = this.environment.getProperty(CONFIG_NAME_PROPERTY);
        return asResolvedSet(property, null);
    }
    return asResolvedSet(ConfigFileApplicationListener.this.names, DEFAULT_NAMES);
}

// ConfigFileApplicationListener#load
private void load(String location, String name, Profile profile, DocumentFilterFactory filterFactory,
                  DocumentConsumer consumer) {
    if (!StringUtils.hasText(name)) {
        for (PropertySourceLoader loader : this.propertySourceLoaders) {
            if (canLoadFileExtension(loader, location)) {
                load(loader, location, profile, filterFactory.getDocumentFilter(profile), consumer);
                return;
            }
        }
        throw new IllegalStateException("File extension of config file location '" + location
                                        + "' is not known to any PropertySourceLoader. If the location is meant to reference "
                                        + "a directory, it must end in '/'");
    }
    Set<String> processed = new HashSet<>();
    // 这里是策略模式,不同的扩展名和加载方式
    for (PropertySourshiyongceLoader loader : this.propertySourceLoaders) {
        for (String fileExtension : loader.getFileExtensions()) {
            if (processed.add(fileExtension)) {
                loadForFileExtension(loader, location + name, "." + fileExtension, profile, filterFactory,
                                     consumer);
            }
        }
    }
}
```

- 这个调用链真的太长了..还都是load方法 我选择放弃,有空再来吧



### 小结

ConfigFileApplicationListener会被ApplicationEnvironmentPreparedEvent触发,开始加载配置文件.

配置文件默认在`classpath:/,classpath:/config/,file:./,file:./config/`四个地址中,且默认文件名为`application`

可以使用`spring.config.additional-location`增加配置地址,也可以直接用`spring.config.location`直接覆盖默认.

`spring.config.name`也可以直接用来覆盖默认的配置文件名.

SpringBoot以`PropertySourceLoader`加载Property类属性,默认的实现有`PropertiesPropertySourceLoader`和`YamlPropertySourceLoader`,此处用了策略模式.

`YamlPropertySourceLoader`可以解析后缀名为`yml`以及`yaml`.

`PropertiesPropertySourceLoader`可以解析`properties`以及`xml`.