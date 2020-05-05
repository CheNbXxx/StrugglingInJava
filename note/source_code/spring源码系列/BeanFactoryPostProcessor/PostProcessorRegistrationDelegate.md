# PostProcessorRegistrationDelegate

> Spring上下文刷新过程中看到，衍生出来单独阅读一下源码。

看名字就知道和Spring中的PostProcessor有关.

该类从上下文刷新的方法中切入,作为一个工具类执行一些上下文的钩子方法,

独立成一个类的原因，可能就是减少主类的代码长度吧.

类中具体方法如下:

![image-20200426221929670](../../../../pic/image-20200426221929670.png)

公开的方法只有两个:

1. invokeBeanFactoryPostProcessors  -  调用BeanFactoryPostProcessor
2. registerBeanPostProcessors - 注册BeanPostProcessor

下面会分别从BeanFactoryPostProcessor和BeanPostProcessor来分析该方法.

---

<!-- more -->

[TOC]



## 相关接口概述

就是钩子方法的上层接口。



### BeanFactoryPostProcessor

- BeanFactoryPostProcessor是SpringBoot中的扩展点之一,IOC容器级别钩子方法.
- **该接口可以在标准初始化之前,自定义修改BeanDefinition,但并不建议在里面对Bean进行初始化.**

 ![image-20200416221805389](../../../../pic/image-20200416221805389.png)



### BeanDefinitionRegistryPostProcessor

- 该接口是`BeanFactoryPostProcessor`的扩展,继承了BeanFactoryPostProcessor.
- **实现该接口可以在标准初始化之前修改Bean的注册信息,比如额外多注册一些Bean之类.**

 ![image-20200416222347446](../../../../pic/image-20200416222347446.png)



### BeanPostProcessor接口

BeanPostProcessor接口是SpringIOC容器中主要的扩展点.

SpringAOP的实现也需要使用该接口,在合适的机会替换原始类,使用代理或者增强的类

该接口主要的方法如下图:

 ![image-20200426223449436](../../../../pic/image-20200426223449436.png)

接口中仅有两个方法,根据方法名应该也直到执行的顺序了.

- 在Bean初始化之前会调用 - postProcessBeforeInitialization.

- 在Bean初始化之后会调用 - postProcessAfterInitialization.

BeanPostProcessor的两个钩子方法的执行穿插在Bean的生命周期中,所以Delegate类并不会有任何调用方法.



## 具体方法


### #invokeBeanFactoryPostProcessors

- 这里应该可以理解成一个委托吧,对BeanFactoryPostProcessors的调用是在PostProcessorRegistrationDelegate这个类中单独实现的.

```java
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
		Set<String> processedBeans = new HashSet<>();
		// 判断BeanFactory是否实现BeanDefinitionRegistry
    	// 只有实现了BeanDefinitionRegistry,才能够调用BeanDefinitionRegistryPostProcessor的相关方法,增加或修改BeanDefinition
		if (beanFactory instanceof BeanDefinitionRegistry) {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
                List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

                for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                        // 按照是否实现BeanDefinitionRegistryPostProcessor,将传入的分成两块
                        if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                            BeanDefinitionRegistryPostProcessor registryProcessor =
                                    (BeanDefinitionRegistryPostProcessor) postProcessor;
                            // 1. 这里是第一个执行的地方
                            // 传入的beanFactoryPostProcessors中的BeanDefinitionRegistryPostProcessor被执行调用
                            registryProcessor.postProcessBeanDefinitionRegistry(registry);
                            registryProcessors.add(registryProcessor);
                   		 } else {
                        	regularPostProcessors.add(postProcessor);
                        } 
				}
            
			// 之后就是执行已经注册的BeanDefinitionRegistryPostProcessor
            // PriorityOrdered -> Ordered -> Others

			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
            // 2. 第二个执行的地方,执行继承了PriorityOrdered的
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
            // 3. 第三个执行的地方,执行继承了Ordered的
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
            // 因为有些BeanDefinitionRegistryPostProcessor的方法中也会注册,所以这里要循环往复.
			boolean reiterate = true;
			while (reiterate) {
                    reiterate = false;
                	// 这里会循环获取BeanDefinitionRegistryPostProcessor,直接BeanFactory中没有还未执行的
                    postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
                    for (String ppName : postProcessorNames) {
                            if (!processedBeans.contains(ppName)) {
                                    currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                                    processedBeans.add(ppName);
                                    reiterate = true;
                            }
                    }
                    sortPostProcessors(currentRegistryProcessors, beanFactory);
                    registryProcessors.addAll(currentRegistryProcessors);
                    // 4. 第四个执行的地方,执行其他的
                    invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
                    currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
            // 此处也是一样,BeanDefinitionRegistryPostProcessors先被调用.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		} else {
            // 如果BeanFactory没有继承registry
            // 直接执行入参中的BeanPostProcessor
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

    	// 接下来是BeanFactory中已经注册的BeanPostProcessor的调用
    	// 还是按照PriorityOrdered -> Ordered -> Others的顺序
  		// 代码省略
		beanFactory.clearMetadataCache();
	}
```



#### 执行顺序小结结

执行的代码就是取一些执行一些并没有什么难度,具体的执行流程如下:

1. 判断入参列表,BeanFactory是否实现了BeanDefinitionRegistry接口,实现执行2,3,没有实现直接跳到4.
2. 执行入参列表中的BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry
3. 执行BeanFactory中已经注册的BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry,已经注册的按照PriorityOrdered -> Ordered -> Others的顺序执行.BeanDefinitionRegistryPostProcesso#
4. 执行上两步收集的BeanDefinitionRegistryPostProcessor#postProcessBeanFactory
5. 执行入参列表中的BeanFactoryPostProcessor#postProcessBeanFactory
6. 执行BeanFactory中已经注册的BeanFactoryPostProcessor#postProcessBeanFactory,已经注册的按照PriorityOrdered -> Ordered -> Others的顺序执行.

**注意,如果传入的BeanFactory没有实现BeanDefinitionRegistry则不会执行postProcessBeanDefinitionRegistry,但是作为BeanFactoryPostProcessor实现的postProcessBeanFactory还是会被调用的.**



#### 重点BeanFactoryPostProcessor类

以下以SpringBoot Servelet Web应用为切入点.

最初的三个入参BeanFactoryPostProcessor:

  ![image-20200425220536920](../../../../pic/image-20200425220536920.png)

其中的前两个在执行流程第一步就是被执行.

`CachingMetadataReaderFactoryPostProcessor`会在BeanDefinitionRegistry中注册一个用于加载`@Configuration`配置类中定义的Bean的BeanDefinitionRegistryPostProcessor.

之后如果没有另外自定义的BeanDefinitionRegistryPostProcessor,SpringBoot中没有默认的继承`PriorityOrdered`和`Ordered`的.

以下是四五步执行的PostProcessor列表.

 ![image-20200425225930957](../../../../pic/image-20200425225930957.png)

其中有`ConfigurationClassPostProcessor`和`ConfigFileApplicationListener$PropertySourceOrderingPostProcessor`比较重要.

接下BeanPostProcessor的钩子方法执行相关类如下.

继承PriorityOrdered的BeanPostProcessor.

 ![image-20200425230316550](../../../../pic/image-20200425230316550.png)

继承Ordered的BeanPostProcessor

 ![image-20200425230338194](../../../../pic/image-20200425230338194.png)

以下是其他的BeanPostProcessor.

其中TestBeanPostProcessor是我个人测试的类.

 ![image-20200425230356693](../../../../pic/image-20200425230356693.png)



其他的再作分析吧.

感觉每个类都是一部短篇小说,但是人物剧情很复杂的那种.

太多了，说不定我读到一半就转行了。





### #registerBeanPostProcessors 

```java
// PostProcessorRegistrationDelegate
public static void registerBeanPostProcessors(
      ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
	// 获取BeanFactory里所有的BeanPostProcessor的实现类
    // 可以看到BeanPostProcessor是从BeanFactory中获取的。
   String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

   // 增加一个BeanPostProcessor
   int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
   beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

   // 四个List,除了internalPostProcessors其他应该都好理解
   List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
   List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
   List<String> orderedPostProcessorNames = new ArrayList<>();
   List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    
    // 分类
    // 此处注意,实现了MergedBeanDefinitionPostProcessor的还会额外放到internalPostProcessors中
   for (String ppName : postProcessorNames) {
          if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                 BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                 priorityOrderedPostProcessors.add(pp);
                 if (pp instanceof MergedBeanDefinitionPostProcessor) {
                    	internalPostProcessors.add(pp);
                 }
          } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
             	orderedPostProcessorNames.add(ppName);
          } else {
             	nonOrderedPostProcessorNames.add(ppName);
          }
   }

    // 和BeanFactoryPostProcessor一样,先注册PriorityOrdered的
   sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
   registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

    // 再注册Ordered的
   List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
   for (String ppName : orderedPostProcessorNames) {
          BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
          orderedPostProcessors.add(pp);
          if (pp instanceof MergedBeanDefinitionPostProcessor) {
             	internalPostProcessors.add(pp);
          }
   }
   sortPostProcessors(orderedPostProcessors, beanFactory);
   registerBeanPostProcessors(beanFactory, orderedPostProcessors);

   //  最后注册所有普通的
   List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
   for (String ppName : nonOrderedPostProcessorNames) {
      BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
      nonOrderedPostProcessors.add(pp);
      if (pp instanceof MergedBeanDefinitionPostProcessor) {
         internalPostProcessors.add(pp);
      }
   }
   registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

   // 这个好像是重新在注册一遍.
   sortPostProcessors(internalPostProcessors, beanFactory);
   registerBeanPostProcessors(beanFactory, internalPostProcessors);

   // 最后注册了ApplicationListenerDetector
   beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}
```

Hhhh，这里就是注册，好像也不需要管顺序不顺序的。

**唯一注意的就是BeanPostProcess在第一步中是从BeanFactory中获取的，所以想要自定义BeanPostProcess生效，就必须在该方法之前注入进BeanFactory，或者在BeanPostProcess中注册另外一个BeanPostProcess。**



#### 重点BeanPostProcessor子类

 ![image-20200426233628495](../../../../pic/image-20200426233628495.png)

上图是SpringBoot Servlet Web应用下的BeanPostProcessor的情况.

感觉好像都蛮重要的,注册顺序就是按照这个顺序注册的. 

以后慢慢分析吧.

做个记录

