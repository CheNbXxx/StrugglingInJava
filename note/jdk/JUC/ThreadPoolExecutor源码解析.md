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



## 执行入口 - execute

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



## 增加一个工作线程 - addWorker

从上文可知addWorker至少有以下三种调用方式:

1. addWorker(notnull,true) -> 表示使用核心线程池运行
2. addWorker(notnull,false) -> 表示使用非核心线程池运行
3. addWorker(null,false) -> RUNNING状态下



```java
// firstTask表示希望执行的任务
// core表示是否是核心线程
// 返回值表示是否添加成功
private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
     	// 外层无限循环
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 下面这个if会拦截下来的情况
            // 当前线程池状态为SHUTDOWN或以上 并且 线程状态不为SHUTDOWN或者firstTask不为空或者workQueue为空
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



**前置检查:**

增加工作线程的前置检查逻辑不复杂，首先检查状态，状态不对直接就退出了。

再来检查当前的线程数，线程数不满足也直接退出了，这里最大的线程数根据入参core来定，如果核心线程则不能超过corePoolSize。

线程数检查通过之后会CAS增加线程数，失败重新检查线程数，成功后需要再次检查线程状态，线程状态改变需要重新检查线程状态。



**状态判断**:

```java
 // addWorker的代码片段
if (rs >= SHUTDOWN &&
    ! (rs == SHUTDOWN &&
       firstTask == null &&
       ! workQueue.isEmpty()))
```

这里的判断可以理解为一下原则：

1. 线程池在SHUTDOWN或以上的状态时不能添加新任务。
2. 线程池状态在SHUTDOWN以上的时候不能添加工作线程。
3. 线程池状态在SHUTDOWN的时候，需要workerQueue不为空的时候才允许添加工作线程。

**firstTask为空的情况，现在猜测是在SHUTDOWN时线程池会添加线程执行workerQueue里积压的任务。**



### 添加失败收尾 - addWorkerFailed

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
            // 尝试终止
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }
```

从工作线程集合中移除以及减少工作线程数目都是非常好理解的。



### 尝试关闭线程池 - tryTerminate

```java
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            // 如果线程池状态为RUNNING就直接返回
            if (isRunning(c) ||
                // 当前状态为TIDYING或者TERMINATED也退出
                runStateAtLeast(c, TIDYING) ||
                // 当前状态为SHUTDOWN并且队列有任务积压也退出
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            
            // 如果当前线程数不为0，就停止空闲的线程
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }
```



### 终止空闲线程 - interruptIdleWorkers

```java
// 入参onlyOne - 只关闭一个线程  
private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 遍历工作线程集合
            for (Worker w : workers) {
                Thread t = w.thread;
                // isInterrupted会检查线程是否关闭
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        // 中断线程
                        t.interrupt();
                    // 非本线程自行中断会进行安全性检查，
                    // 这里直接忽略了安全性检查失败的异常
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }
```

中断线程前的判断有以下两个:

1. 线程是否已经被中断(检查线程的中断标志位)
2. 能都获取到锁(Worker的state是否为0)



## 开启工作线程 - runWorker

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
                // Worker的状态大于STOP的时候必须被中断
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     // 这里真的太骚了！！！
                     // 如果或前面的判断为FALSE，会执行到这里将线程的中断清除，然后在判断
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    // 这里相当于模板模式，提供给子类实现的方法
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        // 真正执行Runnable
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        // 同beforeExecute作用一样
                        afterExecute(task, thrown);
                    }
                } finally {
                    // 数据统计
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            // 执行到这里的情况就是while里的判断为false，即
            // task ==null && getTask()又获取不到新任务
            completedAbruptly = false;
        } finally {
            // Worker退出
            processWorkerExit(w, completedAbruptly);
        }
    }
```





#### Worker退出流程

Worker在执行报错

```java
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        // 这里需要结合runWorker和getTask
        if (completedAbruptly)
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        tryTerminate();

        int c = ctl.get();
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            addWorker(null, false);
        }
    }
```







## 获取任务

```java
    private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            // Are workers subject to culling?
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    
```



## 相关问题



#### 为什么需要对Worker的执行过程上锁 / Worker为什么要继承AQS

以上行为或者结构的原因都是为了中断。

注释中已经说明了，在执行任务期间不允许其他线程中断当前线程，这里可能考虑到每次