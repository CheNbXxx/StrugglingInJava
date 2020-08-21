- 最近在写`多线程执行文件复制`的小demo的时候发现对`线程池`的整个创建步骤还不是特别熟悉,所以就来总结一下吧
- 突然想到`JVM`相关的书还没看完...是不是有点东一榔头西一棒子的感觉,以后要改改了





### ThreadPoolExecutor

- 该类`线程池`中最最核心的一个类,就是`线程池的具体执行者`.

#### Constructor

```java
// 下面是参数最全的构造函数,   
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
```

- **corePoolSize  核心线程池大小**
  - 线程池中核心线程的数目.
  - 当一个任务到来,如果池中当前线程数小于`corePoolSize`则会直接创建新线程执行任务,如果大于`corePoolSize`则会添加到缓存队列`workQueue`中.
  - **线程池一开始并没有线程**,不过有`prestartAllCoreThreads()`可以预创建`corePoolSize`个的线程,以及`prestartCoreThreads()`预创建一个线程.
- **maximumPoolSize 最大线程数**
  - 线程池中可以存在的现成的最大值.
- **keepAliveTime 线程保活时间  /  unit  时间单位** 
  - 两个参数一起确定了线程池中空闲线程的可存活时间.
  - 默认情况下,当前线程数大于`corePoolSize`时该参数才会起作用,若存在线程空闲`keepAliveTime`的时间,则会被终止.
  - `allowCoreThreadTimeOut(boolean)`该方法回使线程池即使线程数目小于`corePoolSize`,也会在固定时间杀死空闲线程,直到线程数为0.
- **workQueue 缓存队列**
  - 存放等待执行的任务的阻塞队列,类型为`BlockingQueue<Runnable>`
  - 三种阻塞队列:
    1. `SynchronousQueue`: 线程移交策略. 具体我也不是太懂.  
    2. `LinkedBlockingQueue`: 无界的缓存等待队列,`maximumPoolSize`失效,当线程数大于`corePoolSize`时会直接放入缓存队列,队列没有上限.
    3. `ArrayBlockigQueue`: 有界的缓存等待队列,使用时必须先指定队列长度,当线程数大于`corePoolSize`后,再来任务会先放入该队列,若队列放满会启用新线程执行任务,**注意是启动线程执行的是当前无法存入缓存队列的线程**,若线程数达到`maximumPoolSize`,缓存队列也放不下时,会抛出错误.
- **ThreadFactory 线程工厂**
  - 是线程池创建线程池调用的类,默认的线程工厂的为`Executors.DefaultThreadFactory`
- **RejectedExecutionHandler 拒绝策略**
  - 当线程池指定的缓存队列已满并且线程池中的线程达到`maximumPoolSize`的时候可以称线程池达到`饱和`,之后再有新任务就会执行`拒绝策略`.
  - `饱和策略`一共有四种:
    1. **ThreadPoolExecutor.AbortPolicy**    默认拒绝策略, 饱和时会丢弃新任务,并抛出`RejectedExecutionException`异常
    2. **ThreadPoolExecutor.DiscardPolicy**    空方法,饱和之后会直接丢弃掉所有的任务,不抛出任何异常.
    3. **ThreadPoolExecutor.DiscardOldestPolicy**    饱和之后删除最先的任务,执行新任务.
    4. **ThreadPoolExecutor.CallerRunsPolicy**    饱和之后回执在当前线程执行任务,安全性不高,不建议使用.

---

- 简单总结,深夜上传....巴萨罗那加油!!!!!!!!!!!!!!!!!!!!!!
