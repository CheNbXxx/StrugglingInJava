## SpringBoot启动流程中的Bean加载过程





以下为SpringApplication类中Bean加载的切入点。

在填充了部分属性后调用BeanDefinitionLoader.load()开始加载BeanDefinition。

```java
// SpringApplication#prepareContext	
load(context, sources.toArray(new Object[0]));

// SpringApplication#load
protected void load(ApplicationContext context, Object[] sources) {
            if (logger.isDebugEnabled()) {
                	logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
            }
    		// 创建并配置BeanDefinitionLoader
            BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
            if (this.beanNameGenerator != null) {
                	loader.setBeanNameGenerator(this.beanNameGenerator);
            }
            if (this.resourceLoader != null) {
                	loader.setResourceLoader(this.resourceLoader);
            }
            if (this.environment != null) {
                	loader.setEnvironment(this.environment);
            }
            loader.load();
}
```



<!-- more -->

---

[TOC]

## 调用链