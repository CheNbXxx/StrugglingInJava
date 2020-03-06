# Spring的事件模型

- 首先明确，Spring中的事件模型是根据观察者模式设计的。



## 基类接口/抽象类

### ApplicationListener   -   监听者

- Spring中所有监听器的顶级接口，所有子类对象必须在onApplicationEvent方法中实现对事件的处理。

```java
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
	/**
	 * Handle an application event.
	 * @param event the event to respond to
	 */
	void onApplicationEvent(E event);
}
```



### ApplicationEventMulticaster   -  事件广播器

```java
public interface ApplicationEventMulticaster {

	// 对监听者的增删操作
    // Bean是原来就在IOC容器中的
	void addApplicationListener(ApplicationListener<?> listener);
	void addApplicationListenerBean(String listenerBeanName);
	void removeApplicationListener(ApplicationListener<?> listener);
	void removeApplicationListenerBean(String listenerBeanName);
	void removeAllListeners();
ApplicationEventMulticaster
	// 事件发布的两个重载方法,ResolvableType表示的是事件的类型
	void multicastEvent(ApplicationEvent event);
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);
}
```



### ApplicationEvent   -   监听事件

- ApplicationEvent是Spring中所有事件的基类，继承自JDK中的EventObject。
- 在EventObject中事件源的基础上又封装了一个timestamp的属性。

```java
public abstract class ApplicationEvent extends EventObject {
	private static final long serialVersionUID = 7099057708183571937L;
	private final long timestamp;
	public ApplicationEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}
	public final long getTimestamp() {
		return this.timestamp;
	}
}
```

Spring中有两个直接继承ApplicationEvent的子类SpringApplicationEvent以及ApplicationContextEvent。

SpringApplicationEvent是于SpringApplication相关的事件。

ApplicationContextEvent则是与ApplicationContext相关的事件。

Spring内置的事件可能就以上两大类。

```java
// ApplicationContextEvent
public ApplicationContextEvent(ApplicationContext source) {
    super(source);
}

// SpringApplication
public SpringApplicationEvent(SpringApplication application, String[] args) {
    super(application);
    this.args = args;
}
```

从构造函数中也可以看出，SpringApplicationEvent的事件源必须是SpringApplication，

而ApplicationContextEvent的事件源必须是ApplicationContext。

一般来说应该是使用ApplicationContextEvent作为事件类型。



### ApplicationEventPublisher   -  事件发布器

- 事件发布动作的顶级接口

```java
@FunctionalInterface
public interface ApplicationEventPublisher {
    // 接口中就定义了两个方法，像List那种顶级接口一样的方法规范接口
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}

	void publishEvent(Object event);
}
```

- ApplicationContext接口继承了该接口，具体的实现在AbstractApplicationContext，其中也会调用广播器ApplicationEventMulicaster作为工具类进行广播。



### 观察者模式角度

从观察者模式的角度来看这些基类/接口，ApplicationListener就是观察者，ApplicationEventMulticaster就是被观察者，所以ApplicationEventMulticaster中会持有ApplicationListener的引用，而ApplicationEvent就是被观察者的各种动作，观察者会对这些动作做出一定的反应。

不知道该怎么描述ApplicationEventMulticaster和ApplicationEventPublisher的关系，ApplicationEventPublisher仅仅是作为一个规范接口来的吧



## 自定义事件发布流程

和观察者模式中一样，事件是由被观察者主动发布的。

Spring中事务的发布流程的最上层方法在AbstractApplicationContext中。

AbstractApplicationContext继承自ConfigurableApplicationContext接口，该接口又继承了ApplicationEventPublisher接口，所以AbstractApplicationContext也实现了publishEvent的方法。

发布的流程很简单：

1. 获取匹配的监听者
2. 调用监听者的触发方法
3. 在父容器继续发布事件

**！！事件的发布是会从子容器传递到父容器的，但不会从父容器到子容器。**

以下为事件发布的流程代码：

```java
  // AbstractApplicationContext
  protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");
      	// 转化事件源类型方便解析和使用  object -> other
		ApplicationEvent applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		}else {
            // 包装为负载的应用事件
			applicationEvent = new PayloadApplicationEvent<>(this, event);
            // 获取事件类型
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

        // earlyApplicationEvent是否为空表示是否需要收集再起的事件，
        // 等事件初始化完成之后统一下发
      	if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}else {
             // 事件发布的主要流程 
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

      	 // 同样在父容器中发布该事件
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			}else {
				this.parent.publishEvent(event);
			}
		}
	}
```



#### 获取广播器

- 返回实例化的广播器，否则就抛出异常。

```java
	// AbstractApplicationContext
	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}
```



#### 事件发布

`multicastEvent`是最终广播的方法，Spring中提供了`SimpleApplicationEventMulticaster`作为默认实现。

```java
	// SimpleApplicationEventMulticaster
	@Override
	public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
        // eventType如果为空，则从event直接获取
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
		Executor executor = getTaskExecutor();
        // 获取所有匹配的ApplicationListeners，并遍历调用
        // 如果存在异步的线程池则采用异步调用，否则未同步
		for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			if (executor != null) {
				executor.execute(() -> invokeListener(listener, event));
			}	else {
				invokeListener(listener, event);
			}
		}
	}
```



##### 获取所有监听者

- 获取所有匹配监听者的方法逻辑在`AbstractApplicationEventMulticaster`中。

- `AbstractApplicationEventMulticaster`中包装了监听者的缓存，**以eventType和sourceType唯一标识一组监听器**，这组监听器被封装为一个ListenerRetriever对象，并默认前置过滤不匹配的监听者。
- `ListenerRetriever`是监听者的封装类，下文会分析该类。

```java
	// AbstractApplicationEventMulticaster
	// 获取所有的监听者集合，方法中就会排除不匹配的监听者。
	protected Collection<ApplicationListener<?>> getApplicationListeners(
			ApplicationEvent event, ResolvableType eventType) {
		// 获取事件源
		Object source = event.getSource();
		Class<?> sourceType = (source !=initialMulticasterinitialMulticaster null ? source.getClass() : null);
         // 构建缓存的key，可以根据这个key获取Abmap中的具体对象。
         // 从此处可知Spring中用事务类型和事件源作为一个事务的唯一标识
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

		// 从缓存中直接获取监听者集合
		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		if (retriever != null) {
            // ListenerRetriever是一种包装类，包装了Listener的集合
			return retriever.getApplicationListeners();
		}

		if (this.beanClassLoader == null ||
				(ClassUtils.isCacheSafe(event.getClass(), this.beanClassLoader) &&
						(sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
			// 此处要上全锁，保证现成的安全性
            synchronized (this.retrievalMutex) {
                  // 再次尝试获取
				retriever = this.retrieverCache.get(cacheKey);
				if (retriever != null) {
					return retriever.getApplicationListeners();
				}
                  // 初始化一个监听器的嗅探器 
				retriever = new ListenerRetriever(true);
                  // 获取相关的监听器
				Collection<ApplicationListener<?>> listeners =
						retrieveApplicationListeners(eventType, sourceType, retriever);
                  // 添加到缓存
				this.retrieverCache.put(cacheKey, retriever);
				return listeners;
			}
		}else {
			// No ListenerRetriever caching -> no synchronization necessary
             // 没有监听器缓存的情况，没有同步代码的必要。
			return retrieveApplicationListeners(eventType, sourceType, null);
		}
	}
```



###### retrieveApplicationListeners

- 在监听者的缓存中没有找到或者不使用缓存的时候使用该方法获取匹配的监听者集合。

```java
	private Collection<ApplicationListener<?>> retrieveApplicationListeners(
			ResolvableType eventType, @Nullable Class<?> sourceType, @Nullable ListenerRetriever retriever) {
		List<ApplicationListener<?>> allListeners = new ArrayList<>();
		Set<ApplicationListener<?>> listeners;
		Set<String> listenerBeans;
		synchronized (this.retrievalMutex) {
             // defaultsRetriever中的监听器会在初始化时从SpringApplication中导入
			listeners = new LinkedHashSet<>(this.defaultRetriever.applicationListeners);
			listenerBeans = new LinkedHashSet<>(this.defaultRetriever.applicationListenerBeans);
		}

		// 此时的listeners就是默认得到嗅探者中的监听器，在此种先过滤这部分。
		for (ApplicationListener<?> listener : listeners) {
            // 判断事件是否支持事件类型和来源类型
			if (supportsEvent(listener, eventType, sourceType)) {
				if (retriever != null) {
					retriever.applicationListeners.add(listener);
				}
				allListeners.add(listener);
			}
		}

		// 根据bean名称判断是否支持
		if (!listenerBeans.isEmpty()) {
             // 获取beanFactory对象 
			ConfigurableBeanFactory beanFactory = getBeanFactory();
             // 遍历bean名称
			for (String listenerBeanName : listenerBeans) {
				try {
                       // 根据bean名称判断是否支持，此时并未初始化
                       // 根据BeanFactory获取的type判断是否支持是否支持该时间类型
					if (supportsEvent(beanFactory, listenerBeanName, eventType)) {
                           // 获取监听者对象，可能包含对象的初始哈
						ApplicationListener<?> listener =
								beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                           // 上面已经判断过是否支持时间类型
                           //  此时多判断的是否支持来源类型
						if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
                            // 因为现在beanName都是defaultRetriever中的，所以确认之后需要添加到新建的中
							if (retriever != null) {
								if (beanFactory.isSingleton(listenerBeanName)) {
									retriever.applicationListeners.add(listener);
								}
								else {
									retriever.applicationListenerBeans.add(listenerBeanName);
								}
							}
							allListeners.add(listener);
						}
					}
                      // 监听器不支持类型,需要删去
					else {
						Object listener = beanFactory.getSingleton(listenerBeanName);
						if (retriever != null) {
							retriever.applicationListeners.remove(listener);
						}
						allListeners.remove(listener);
					}
				}
				catch (NoSuchBeanDefinitionException ex) {
					// 忽略异常
				}ApplicationListener
			}
		}
		// 按照order排序
		AnnotationAwareOrderComparator.sort(allListeners);
		if (retriever != null && retriever.applicationListenerBeans.isEmpty()) {
			retriever.applicationListeners.clear();
			retriever.applicationListeners.addAll(allListeners);
		}
		return allListeners;
	}
```



##### 发布对应的事件

- 事件的发布就是获取所有监听者之后，再遍历调用监听者的公有接口方法。
- 三级调用链下去，最终调用ApplicationListener的onApplicationEvent方法。

```java
// SimpleApplicationEventMulticaster
@Override
public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
	ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
	Executor executor = getTaskExecutor();		
    for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
	     if (executor != null) {
				executor.execute(() -> invokeListener(listener, event));
		}
		else {
				invokeListener(listener, event);
		}
	}
}
// SimpleApplicationEventMulticaster
protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
    // 异常处理器
   ErrorHandler errorHandler = getErrorHandler();
   if (errorHandler != null) {
      try {
         doInvokeListener(listener, event);
      }catch (Throwable err) {
         errorHandler.handleError(err);
      }
   }else {
      doInvokeListener(listener, event);
   }
}

// SimpleApplicationEventMulticaster
private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
    try {
        // 应该是全部发布代码中最核心的部分了，调用每个监听器的onApppliucationEvent方法
        // 以当前事件为入参
        listener.onApplicationEvent(event);
    }catch (ClassCastException ex) {
        String msg = ex.getMessage();
        if (msg == null || matchesClassCastMessage(msg, event.getClass())) {
            Log logger = LogFactory.getLog(getClass());
            if (logger.isTraceEnabled()) {
                logger.trace("Non-matching event type for listener: " + listener, ex);
            }
        }else {
            throw ex;
        }
    }
}
```







## SpringBoot启动过程中的事件

首先所有在SpringBoot启动过程中的事件发布，最上层方法调用都来自`SpringApplicationRunListeners`，该类就是`SpringApplicationRunListener`的一个集合。

```java
class SpringApplicationRunListeners {
	private final Log log;
    // 以一个简单List的形式存放所有的SpringApplicationRunListener
	private final List<SpringApplicationRunListener> listeners;
    ...
```

事件发布过程也很简单

```java
// SpringApplication
// ApplicationStartingEvent事件的发布
// getRunListeneres方法中会初始化EventPublishingRunListener，
// 以当前的SpringApplication为入参
SpringApplicationRunListeners listeners = getRunListeners(args);
listeners.starting();

// SpringApplicationRunListeners
public void starting() {
    // 遍历调用，简单粗暴
    for (SpringApplicationRunListener listener : this.listeners) {
		listener.starting();	
    }
}
```



### SpringApplicationRunListener

- SpringBoot启动过程中，也会触发很多的事件，区别于我们自定义的事件发布，Spring启动流程中的事件发布的顶级接口为`SpringApplicationRunListener`。

```java
// 接口中包含了容器启动各个流程状态的触发方法
// 具体的实现包含在子类EventPublishingRunListener中
public interface SpringApplicationRunListener {
    // 容器启动 cubao
	default void starting() {}
    // 容器环境准备完成
	default void environmentPrepared(ConfigurableEnvironment environment) {}
    // 上下文准备完成
	default void contextPrepared(ConfigurableApplicationContext context) {}
    // 上下文加载完成
	default void contextLoaded(ConfigurableApplicationContext context) {}
    // 容器启动完成
	default void started(ConfigurableApplicationContext context) {}
    // 容器正在运行AbAb
	default void running(ConfigurableApplicationContext context) {}
    // 容器启动失败
	default void failed(ConfigurableApplicationContext context, Throwable exception) {}
}
```

在Run方法中会通过工厂加载模式加载所有的`SpringApplicationRunListener`，并全部封装在`SpringApplicationRunListeners`中。

SpringApplicationRunListener在Spring框架内部的唯一实现就是EventPublishingRunListener。

EventPublishRunListener中封装了`SimpleApplicationEventMulticaster`。

`SimpleApplicationEventMulticaster`是Spring中默认的事件发布器的实现，`AbstractApplicationContext`内部也有该引用，借此实现事件的发布。

EventPublishingRunListener作为SpringBoot启动过程中的事件相关工具类。

```java
	public EventPublishingRunListener(SpringApplication application, String[] args) {
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
        // 构造函数中会将当前的SpringApplications中的监听器导入到封装的广播器中
		for (ApplicationListener<?> listener : application.getListeners()) {
			this.initialMulticaster.addApplicationListener(listener);
		}
	}
```



## ListenerRetriever

- ListenerRetriever是监听器集合的封装类，在AbstractApplicationContext中以此为V进行缓存。

```java
private class ListenerRetriever {
    	// 采用两种集合的方式保存监听者对象
    	// 直接保存对象和保存bean名称
    	// 最终会保存在applicationListeners，
        // applicationListenerBeans中的beanName在经过一次getApplicationListeners后会加入到上面
		public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();
		public final Set<String> applicationListenerBeans = new LinkedHashSet<>();

    	// 本来以为该字段的意思是是否需要前置过滤
   		// 后来看应该是是否已经经过前置过滤
		private final boolean preFiltered;
		
    	// 没有默认的构造函数必须要指定是否前置过滤
		public ListenerRetriever(boolean preFiltered) {
			this.preFiltered = preFiltered;
		}

		public Collection<ApplicationListener<?>> getApplicationListeners() {
			List<ApplicationListener<?>> allListeners = new ArrayList<>(
					this.applicationListeners.size() + this.applicationListenerBeans.size());
             // allListeners此时包含了全部applicationListeners的对象
			allListeners.addAll(this.applicationListeners);
             // 遍历bean名称的集合
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					try {
                          // 从beanFactory中获取对应bean名称的对象
						ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                          // 如果需要前置过滤并且当前集合中不包含该listener，就将其加入集合。
						if (this.preFiltered || !allListeners.contains(listener)) {
							allListeners.add(listener);
						}
					}catch (NoSuchBeanDefinitionException ex) {
						// 此处会忽略未找到该监听器的异常。
					}
				}
			}
            // 未经过前置过滤或者applicationListenerBeans不为空时，需要进行排序
            // 有序监听时就需要按照@Order先进行排序在执行。
			if (!this.preFiltered || !this.applicationListenerBeans.isEmpty()) {
				AnnotationAwareOrderComparator.sort(allListeners);
			}
			return allListeners;
		}
	}
```

- 采用两种方式保存监听者集合的原因<font size=2>（看了retrieveApplicationListeners的代码才知道，看来我的理解还很不够）</font>：

  **Spring中不仅仅只有单例模式的bean，如果遇到原型模式就需要重新进行初始化。**



## ErrorHandler 异常处理接口

```java
@FunctionalInterface
public interface ErrorHandler {
   /**
    * Handle the given error, possibly rethrowing it as a fatal exception.
    */
   void handleError(Throwable t);
}
```

- 接收一个throwable类型的入参，void类型的返回，可以理解为方法中消化了这个异常。





## 自定义监听器

实现自定义的事件监听有两种方式

1. @EventListener

```java
@Component
@Scope("singleton")
public class TestApplicationListener{
    // 方法上标注EventListener就可以监听方法入参类型的事件
    @EventListener
    public void onApplicationEvent(ApplicationEvent event) {
       ...
    }
}
```

2. 继承`ApplicationListener`或其子类

```java
// 实现ApplicationListener接口
// 接口中的泛型类型就是想要监听的事件类型
@Component
@Scope("singleton")
public class TestApplicationListener implements App发布的流程很简单就是获取监听器并调用实现的接口方法。licationListener<ContextRefreshedEvent>{
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
       ...
    }
}
```



- 两种方式都可以通过标注为Bean的方式使其生效。
- 如果采用实现ApplicationListener接口的方法还可以使用工厂加载机制，在`spring.factories`中声明，在SpringApplication的构造函数中会获取所有ApplicationListener的方法。
- 相对于标注为Bean的情况，工厂加载机制会更早的创建实例，如果监听的是ApplicationEvent，通过工程加载机制就能监听到refresh方法之前的事件。



## 总结

1. 事件模型包含基本的三类对象：监听者，监听事件， 事件广播器，整体依托于观察者模式。
2. 对于自定义的事件来说，Spring实现了ApplicationEventPublisher提供消息发布的功能，间接调用Multicaster。
3. 对于SpringBoot启动过程中的事件来说，SpringApplicationRunListeners才是其最上层的类。