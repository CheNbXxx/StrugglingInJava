# ThreadPoolExecutor源码解析

> ThreadPoolExecutor就是JDK中的线程池实现。

池化技术是重复利用资源，减少创建和删除消耗的一种技术，类似的还有内存池，连接池等概念。

Java中的线程映射了内核中的一个轻量级线程，所以创建和销毁都会带来不小的消耗。

以一个线程池的概念，生成多个线程重复利用，就是该类对性能优化的的核心思想。





## 构造函数

这个基本是面试都会问的问题了吧，非常重要，因为设定不同的入参使我们控制线程池执行方式的唯一方法了。

 ![image-20200922220330699](/home/chen/Pictures/image-20200922220330699.png)

以上就是ThreadPoolExecutor类内所有的构造函数。

参数的含义如下:

| 参数名          | 含义             |
| --------------- | ---------------- |
| corePoolSize    | 核心池的线程数量 |
| maximumPoolSize | 最大线程数量     |
| keepAliveTime   | 线程保活时间     |
| TimeUnit        | 保活的时间单位   |
| workQueue       | 工作队列         |
| threadFactory   | 线程工厂         |
| handler         | 拒绝策略。       |

### corePoolSize

线程池的核心线程数量，





## ctl 线程池状态和线程数

这里是JDK的线程池中一个非常亮眼的设计，以ctl一个32位整型表示了两个线程池参数。

这么设计的意义就是使线程池的状态和线程数的设置可以同时进行，保证彼此的一致性。



 ![image-20200922221847131](/home/chen/Pictures/image-20200922221847131.png)



如上图所示，ctl以一个AtomicInteger类型表示，高3位表示当前线程池的状态，低29位表示线程的数目。

COUNT_BITS是表示线程数目的位数，也就是29位，这里也可以看出来，线程池的线程上限就是2^29个。

CAPACITY表示线程的数目上线，也用于求线程数以及线程状态，具体可以看下面`runStateOf`以及`workerCountOf`两个方法。



再下面就是线程池的状态了:



| 状态标示   | 状态含义                                                     |
| ---------- | ------------------------------------------------------------ |
| RUNNING    | 线程池正常运行，可以接收新的任务并执行。                     |
| SHUTDOWN   | 线程池已经关闭，停止接受新的任务，但是排队中的任务以及执行中的任务都还要执行。 |
| STOP       | 线程池正式关闭，不仅不接受新的任务，排队中以及执行中的任务都需要取消或者中断。 |
| TIDYING    |                                                              |
| TERMINATED |                                                              |



## 执行入口

`ThreadPoolExeuctor.execute`就是线程池的核心方法，以一个`Runnable`为入参，向线程池添加任务。

以下差不多就是`execute()`方法的全部代码:

 ![image-20200922221155365](/home/chen/Pictures/image-20200922221155365.png)

**第一步获取ctl变量,判断是否可以使用核心线程池**，如果线程池的工作线程数量小于**corePoolSize**，就直接调用addWorker方法添加任务，执行成功就直接return。

添加失败或者当前线程数已经大于**corePoolSize**就开启第二步。

**第二步判断当前线程池是否为RUNNING状态**，如果是则尝试将任务添加到工作队列。

这里我个人一致有一个理解上的误区，最开始以为再超过corePoolSize之后会继续增加线程到maximumPoolSize，才放入workQueue。

因为需要防止超过corePoolSize的任务，所以workQueue就很关键了。

添加到workQueue成功之后还会再次检查当前线程池的状态，如果状态不为RUNNING，则移除当前任务，并执行拒绝逻辑。

如果状态为RUNNING，或者不为RUNNING但是当前任务删除失败，则调用`addWorker(null,false)`

**第三步则是线程不是RUNNING状态或者入工作队列失败的情况下**，重新尝试添加任务，以非核心线程池运行，

失败则直接执行拒绝逻辑。







### addWorker - 添加任务到集合

从上文可知addWorker至少有以下三种调用方式:

1. addWorker(notnull,true) -> 表示使用核心线程池运行
2. addWorker(notnull,false) -> 表示使用非核心线程池运行
3. addWorker(null,false) -> RUNNING状态下

```java
// firstTask表示希望执行的任务
// core表示是否以核心线程执行
private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
     	// 外层无限循环
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 下面这个判断语句的放行的情况有
            // 1. rs为RUNNING，正常运行的线程池肯定自动放行
            // 2. rs为SHUTDOWN并且firstTask为空并且workQueue不为空
            // 这个第二个放行条件看的有点奇怪，先过看下面的
            // 满足以上条件就会退出，表示任务添加失败
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;
	
            // 内层循环
            for (;;) {
                // 获取当前线程数
                int wc = workerCountOf(c);
                // 1.  当前线程数大于等于容量上限
                // 2.  以核心线程运行时大于corePoolSize
                // 3.  以非核心线程运行时大于maximumPoolSize
                // 满足以上条件就会退出，表示任务添加失败
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                // 递增线程池的线程数量
                // 成功就退出
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                // 递增失败表示执行期间ctl被改变了,可能是状态或者线程数变了
                c = ctl.get();  // Re-read ctl
                // 如果是状态变了,就重新执行外面的循环
                // 如果是线程数目变了就执行内存循环就好了
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }
    
    	// 上面两个循环，外层循环检查状态，内存循环检查线程数。

    	// 这两个从名字也可以看出来，任务启动和添加是否成功
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            // 包装新的任务
            w = new Worker(firstTask);
			// 创建新Worker的时候就会创建一个新线程
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                // 上锁
                mainLock.lock();
                try {
                    // 重新检查线程池状态
                    int rs = runStateOf(ctl.get());
                    if (rs < SHUTDOWN ||
                        // 这里有个奇怪的点，
                        // 为什么线程池希望关闭的状态并且firstTask为Null时还需要添加任务到集合
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        // 添加到任务集合中
                        workers.add(w);
                        int s = workers.size();
                        // 重新计算最大的线程数
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                // 线程创建成功
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            // 线程没有启动成功的一些收尾工作
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }
```



addWorker失败的收尾逻辑如下:

```java
   private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 从工作集合中移除
            if (w != null)
                workers.remove(w);
            // 减去任务数
            decrementWorkerCount();
            // 尝试停止
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }
```





## 任务执行

上文说到任务以Runnable形式接收，包装成Worker并添加到workers集合，添加成功开启线程执行任务。

 ![image-20200923071519311](/home/chen/Pictures/image-20200923071519311.png)

上图就是Worker的run方法，直接调用的**ThreadPoolExecutor.runWorker()**来执行当前任务。

以下就是runWorker的全部源码.

```java
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        // 取出任务并置空原任务。
        Runnable task = w.firstTask;
        w.firstTask = null;
        // 允许中断
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            // 这里是一直循环获取任务的
            // task==null时，会从getTask()方法获取下一个任务
            while (task != null || (task = getTask()) != null) {
                w.lock();
            	// 触发线程中断的条件
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     // interrupted()方法不仅仅返回线程的中断状态，还会清除线程的中断标记
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

```





