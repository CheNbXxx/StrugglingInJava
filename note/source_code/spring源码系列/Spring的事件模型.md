# Spring的事件模型





## 基类接口对象



### ApplicationListener   -   监听者

- Spring中所有监听器类型的基类。

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

- 较为常见的子类有GenericApplicationListener



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



### ApplicationEventMulticaster   -  事件广播器

- 发布事件的途径之一。
- 在AbstractApplicationContext中会调用该方法进行事件的发布。

```java
public interface ApplicationEventMulticaster {

	// 对监听者的增删操作
    // Bean是原来就在IOC容器中的
	void addApplicationListener(ApplicationListener<?> listener);
	void addApplicationListenerBean(String listenerBeanName);
	void removeApplicationListener(ApplicationListener<?> listener);
	void removeApplicationListenerBean(String listenerBeanName);
	void removeAllListeners();

	// 事件发布的两个重载方法,ResolvableType表示的是事件的类型
	void multicastEvent(ApplicationEvent event);
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);
}
```



### ApplicationEventPublisher   -  事件发布器

- 发布事件的主要接口方法。

```java
@FunctionalInterface
public interface ApplicationEventPublisher {
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}

	void publishEvent(Object event);
}
```

- ApplicationContext接口继承了该接口，具体的实现在AbstractApplicationContext，其中也会调用广播器ApplicationEventMulicaster作为工具类进行广播。







## 事件发布流程

Spring中事务的发布流程主要在AbstractApplicationContext中。<font size=2>(AbstractApplicationContext继承了ConfigurableApplicationContext接口，该接口又继承了ApplicationEventPublisher接口。</font>

以下为事件发布的流程代码：

```java
  // AbstractApplicationContext
  protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");
      	// 转化事件源类型方便解析和使用
		ApplicationEvent applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		}
		else {
            // 包装为负载的应用事件
			applicationEvent = new PayloadApplicationEvent<>(this, event);
            // 获取事件类型
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

		// Multicast right now if possible - or lazily once the multicaster is initialized
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else {
             // 事件发布的主要流程 
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

      	 // 同样在父容器中发布该事件
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			}
			else {
				this.parent.publishEvent(event);
			}
		}
	}
```



#### 获取广播器

- 获取不到就抛异常，否则返回已经实例化的广播器。

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



#### 发布事件

`multicastEvent`是最终广播的方法，Spring中提供了`SimpleApplicationEventMulticaster`作为默认实现。

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
```



##### 获取所有监听者

```java
	// AbstractApplicationEventMulticaster
	// 获取所有的监听者集合，方法中就会排除不匹配的监听者。
	protected Collection<ApplicationListener<?>> getApplicationListeners(
			ApplicationEvent event, ResolvableType eventType) {
		// 获取事件源
		Object source = event.getSource();
		Class<?> sourceType = (source != null ? source.getClass() : null);
         // 构建缓存的key，可以根据这个key获取map中的具体对象。
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
                  // 根据 
				this.retrieverCache.put(cacheKey, retriever);
				return listeners;
			}
		}
		else {
			// No ListenerRetriever caching -> no synchronization necessary
             // 没有监听器缓存的情况，没有同步代码的必要。
			return retrieveApplicationListeners(eventType, sourceType, null);
		}
	}
```

- AbstractApplicationEventMulticaster中包装了监听者的缓存，**以eventType和sourceType唯一标识一组监听器**，这组监听器被封装为一个ListenerRetriever对象，并默认前置过滤不匹配的监听者。
- `ListenerRetriever`是监听者的封装类，



###### retrieveApplicationListeners

- 在监听者的缓存中没有找到或者不使用缓存的时候使用该方法获取匹配的监听者集合。

```java
	private Collection<ApplicationListener<?>> retrieveApplicationListeners(
			ResolvableType eventType, @Nullable Class<?> sourceType, @Nullable ListenerRetriever retriever) {
		List<ApplicationListener<?>> allListeners = new ArrayList<>();
		Set<ApplicationListener<?>> listeners;
		Set<String> listenerBeans;
		synchronized (this.retrievalMutex) {
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
				}
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