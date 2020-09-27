# ThreadPoolExecutor

> ThreadPoolExecutor就是JDK中的线程池实现。



池化技术是重复利用资源，减少创建和删除消耗的一种技术，类似的还有内存池，连接池等概念。

Java中的线程映射了内核中的一个轻量级线程，所以创建和销毁都需要切换到内核态，都会带来不小的消耗。

以一个线程池的概念，生成并持有多个线程对象，重复利用，就是该类对性能优化的的核心思想。

在日常开发过程中免不了会与线程池打交道，知晓点源码说不定还能帮我们快速定位线上问题。



## 源码解析

### 构造函数

这个基本是面试都会问的问题了吧，非常重要，因为设定不同的入参是我们控制线程池执行方式的最主要的方法。

 ![image-20200922220330699](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200922220330699.png)

以上就是ThreadPoolExecutor类内所有的构造函数。

以下是参数最完整的一个:

 ![image-20200927210322840](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200927210322840.png)

参数的含义如下:

| 参数名          | 含义                   |
| --------------- | ---------------------- |
| corePoolSize    | 核心池的线程数量       |
| maximumPoolSize | 最大线程数量           |
| keepAliveTime   | 空闲线程保活时间       |
| TimeUnit        | 空闲线程保活的时间单位 |
| workQueue       | 等待队列               |
| threadFactory   | 线程工厂               |
| handler         | 拒绝策略               |







### ctl 线程池状态和线程数

这里是ThreadPoolExecutor中一个非常亮眼的设计，**以一个32位整型表示了两个线程池参数。**

这样的设计使线程池的状态和线程数的设置可以同时进行，保证彼此的关联性，而且位运算的效率也不错。



 ![image-20200922221847131](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200922221847131.png)



如上图所示，**ctl是一个AtomicInteger类型的对象，高3位表示当前线程池的状态，低29位表示线程的数目。**

COUNT_BITS是表示线程数目的位数，也就是29位，这里也可以看出来，线程池的线程上限就是2^29个。

CAPACITY表示线程的数目上线，也用于求线程数以及线程状态，具体可以看下面`runStateOf`以及`workerCountOf`两个方法。





接下来看线程池的状态:

 ![image-20200924233552756](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200924233552756.png)



整理如下:

| 状态标示   | 状态含义                                                     |
| ---------- | ------------------------------------------------------------ |
| RUNNING    | 线程池正常运行，可以接收新的任务并执行。                     |
| SHUTDOWN   | 线程池已经关闭，停止接受新的任务，但是排队中的任务以及执行中的任务都还要执行。 |
| STOP       | 线程池正式关闭，不仅不接受新的任务，排队中以及执行中的任务都需要取消或者中断。 |
| TIDYING    | 整理过渡状态，工作线程为0时就需要调用terminated()方法。      |
| TERMINATED | terminated()方法执行完毕就是终止状态。                       |

以上状态非常关键，因为不论是添加任务，执行任务，都需要先检查线程池的状态。

<img src="https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/未命名文件 (4).png" style="zoom:50%;" />



### 其他相关组件对象

#### Worker

Worker是ThreadPoolExecutor的内部类，同时也是**线程池中具体的工作线程的持有者。**

线程池中的所有工作线程都保存在以下的成员变量中:

 ![image-20200926222616980](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200926222616980.png)



Worker作为ThreadPoolExecutor的内部类，自身继承了AbstractQueuedSynchronizer，并实现了Runnable接口。

以下是Worker中的内部变量:

 ![image-20200926222820062](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200926222820062.png)

除了具体的工作线程Thread外，还有初始任务以及完成的任务计数。

其实Worker本身就是一个Runnable，交由thread来执行，但是它本身又包含了另外需要执行的Runnable，也就是firstTask<font size=2>(这里好像有点乱)</font>。

在添加工作线程的时候可以选择一起添加初始任务(addWorker(runnable,boolean))，那么在runWorker中就会先执行firstTask而不是直接去getTask。

**没有firstTask的调用则表示是直接添加工作线程消费阻塞队列中的任务。(addWorker(null,boolean))**



下面是Worker中有关于AQS的方法实现:

 ![image-20200926225406590](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200926225406590.png)

可以看到这里的上锁和解锁就是state在0,1之间的变化。

因为本身就是单线程工作所以也不存在竞争的问题，**这里的上锁的更像是表示线程的忙碌标志。**

下文会看到runWorker中在执行某个具体的任务之前都会先上锁，也就表示了线程正在执行一个任务，在忙碌状态。



### 添加任务入口 - execute

该方法用于向线程池添加新的任务，是ThreadPoolExecutor中最上层的方法。



方法源码如下:

 ![image-20200922221155365](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200922221155365.png)



**第一步获取ctl变量,判断是否可以使用核心线程池**，如果线程池的工作线程数量小于**corePoolSize**，就直接调用addWorker方法添加任务，执行成功就直接return。

添加失败或者当前线程数已经大于**corePoolSize**则进入第二步。

**第二步判断当前线程池是否为RUNNING状态**，如果是则尝试将任务添加到工作队列。

<font size=2>(这里我个人一直有一个理解上的误区，最开始以为再超过corePoolSize之后会继续增加线程到maximumPoolSize，才放入workQueue)</font>

任务添加到workQueue成功之后还会再次检查当前线程池的状态，**如果状态不为RUNNING，则移除当前任务，并执行拒绝逻辑。**

如果状态为RUNNING或者移除任务失败，那么可能迫于无奈的尝试去执行入队的任务，此时会去检查线程池中线程的数目，

当线程数目为0时添加一个不带初始任务的非核心线程去消费阻塞队列中的任务。

**第三步是线程不是RUNNING或者入队列失败的情况，**会直接调用addWorker尝试以非核心线程执行当前任务，如果还失败则执行拒绝策略。

addWorker中也会前置检查，比如当前线程为SHUTDOWN但是因为firstTask不为空，所以会直接返回false，然后执行拒绝策略。



另外的方法中在offer到阻塞队列的前后都会对线程池的状态进行判断。



以下为线程池整体的任务添加逻辑:

 <img src="https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/未命名文件 (3).png" style="zoom:57%;" />

以上就是线程池添加新任务最外层的逻辑，可能也是面试问的最多的地方吧。

添加的任务最终有以下几个去向：

1. 被拒绝策略拒绝
2. 被放入阻塞队列
3. 直接开启新的核心线程执行
4. 入队失败后尝试开启非核心线程执行





### 添加任务 - addWorker

addWorker方法就是希望添加一个新的工作线程到线程池中。

```java
// firstTask表示希望执行的任务
// core表示是否是核心线程，false表示无所谓而不一定
// 返回值表示是否添加成功
private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
     	// 外层的状态检查循环
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            // 能增加工作线程的情况
            // 1. 当前状态为RUNNING
            // 2. 当前状态为SHUTDOWN，但是阻塞队列不为空
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;
            // 内层线程数检查循环
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
                // 成功就退出循环
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
    
    	// 上面两个循环，外层循环检查状态，内存循环检查线程数
    	// 通过之后开始具体的添加逻辑

    	// 这两个从名字也可以看出来，任务启动和添加是否成功
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            // 包装新的Worker
            w = new Worker(firstTask);
			// 创建新Worker的时候就会创建一个新线程
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                // 上锁
                mainLock.lock();
                try {
                    // 重新获取线程池状态
                    int rs = runStateOf(ctl.get());
                    // 在RUNNING的状态下
                    // 或者SHUTDOWN时不添加任务只添加线程
                    if (rs < SHUTDOWN ||
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
                // 线程添加成功
                if (workerAdded) {
                    // 直接开启线程
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



添加任务的整个逻辑并不复杂，不过有多次的状态检查。

前置检查之后会把会把**入参Runnable包装为Worker对象，然后交给Thread对象执行。**



**前置检查:**

增加工作线程的前置检查就更简单了，首先检查状态，状态不对直接就退出了。

再来检查当前的线程数，线程数不满足也直接退出了，这里最大的线程数根据入参core来定，如果增加的是核心线程则不能超过corePoolSize。

线程数检查通过之后会CAS增加线程数，失败重新检查线程数，成功后需要再次检查线程状态，线程状态改变需要重新检查线程状态。



通过前置检查可以确定下面两个情况:

1. 添加工作线程的时机，**只能在线程池状态为RUNNING或者SHUTDOWN但工作线程不为空的情况下。**
2. **线程数永远不能大于maximumPoolSize**



对于workers集合来说，它保存的是已经创建的Worker对象，而在executor方法中的workerQueue中保存的是未包装的Runnable对象。



#### 添加失败收尾 - addWorkerFailed

该方法在addWorker失败之后调用，比如添加失败或者启动失败等原因。

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

另外的添加Worker失败之后还会调用tryTerminate来检查线程池的状态，判断线程池是否应该terminate。



### 开启工作线程 - runWorker

上文说到任务以Runnable形式接收，包装成Worker并添加到workers集合，添加成功开启线程执行任务。

以下就是worker的run方法，也就是工作线程执行逻辑入口：

 ![image-20200923071519311](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200923071519311.png)

很干脆的只有调用`runWorker`方法。



以下就是runWorker()方法的全部源码.

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
                    // 以上的整个判断都是为了保证以下两点：
                    // 1. 线程池状态在STOP以上的时候必须发生中断
                    // 2. 不在STOP以上的时候，清除中断状态
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
                    // 这里把Task置空，保证任务只被执行一次
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

整个`runWorker()`方法可以看做一个大的while循环，所以是由while中的条件来控制线程的生命周期。



**首次调用会执行firstTask，如果firstTask为空那么会调用getTask获取阻塞队列中的任务。**

接下来就是while循环中对线程状态的判断，保证了以下两个关键点:

1. 线程池在STOP的状态下，当前线程必须中断。
2. 如果线程池不在STOP状态，那么线程就不能被中断，正式执行Runnable之前就需要清除中断状态。

现成的执行前后有`beforeExecute`以及`afterExecute`两个钩子方法，这两个是模板方法，子类可以实现以复制或增强线程池任务执行。

还有就是知道了每个Worker都会记录执行完成的任务数。



整个任务执行流程中值得关注的就是Worker对中断的处理。

**第一个确保的就是具体的任务执行期间是不允许中断的。**

**另外的保证就是只有STOP状态下，工作线程才会在获取到任务的状态下主动退出。**



**另一个值得关注的点是锁问题，为什么单线程执行需要w.lock()方法上锁。**

这个其实也和中断有关，在`interruptIdleWorkers`方法中可以看到，线程池中对于空闲的定义就是可以获取到锁，所以这里的上锁也就表明当前工作线程正忙。



如果firstTask为空并且getTask也没有获取到任务(不一定是阻塞队列为空)，那么工作线程就会进入退出流程。

#### Worker退出流程

```java
// Worker就是希望退出的线程
// completedAbruptly表示是否因为异常退出，
// 这个可以结合runWorker的代码，如果正常情况下没有获取到任务而退出，completedAbruptly会是false
private void processWorkerExit(Worker w, boolean completedAbruptly) {
        // 突然退出的的情况下需要减去WorkerCount
    	// 非突然退出的时候在getTask就会减
        if (completedAbruptly)
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 统计完成任务数
            completedTaskCount += w.completedTasks;
            // 删除工作线程
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }
		// 尝试停止，这个方法很常见了
        tryTerminate();

        int c = ctl.get();
    	// 状态小于STOP,也就是SHUTDOWN和RUNNING
        if (runStateLessThan(c, STOP)) {
            // 正常退出的情况下
            if (!completedAbruptly) {
                // allowCoreTreadTimeOut表示空闲的核心线程是否需要收回
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                // 如果阻塞队列中还有任务就必须留有线程执行
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                // 线程足够就不需要添加
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            // 添加一个非核心线程
            addWorker(null, false);
        }
    }
```

线程池会统计总共执行了多少任务，每个线程都是在准备退出之前才会统计到线程池中

一个Worker在异常退出的时候会替换一个新的非核心线程进来，如果一个Worker正常退出则会作以下判断。

1. 核心线程空闲时是否需要回收，不需要的话，线程数就要大于corePoolSize，此时需要替换一个新Worker
2. 阻塞队列中是否还留有任务，还有的话就必须要留下一个线程来执行。

可以看到的是在线程非正常退出的时候，如果线程池状态正常就会添加一个新的线程。





### 获取任务 - getTask

该方法从名字来看就是获取任务的方法，准确来说是**从阻塞队列获取任务**的方法。

但其中还包含了**对线程池状态的检测，以及对线程生命周期的控制**，这里的控制指的是getTask如果返回null，那么`runWorker`中的while循环就退出了。

```java
    private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 直接返回空并减去工作线程数的情况
            // 1. 线程池状态为SHUTDOWN，并且工作线程为空
            // 2. 线程池状态为STOP以上
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                // 这里已经减去了工作线程数
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            // 线程是否需要检测超时
            // wc大于corePoolSize的时候就相当于允许超时，允许淘汰
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
			// getTask的返回控制线程是否需要淘汰
            // 1. 线程数超过maximumPoolSize
            // 2. 
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                // 根据
                // take()方法是可以检测中断的，所以不会造成死等
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



获取任务的流程简述如下:

首选检查当前线程池状态是否允许获取，只有RUNNING以及SHUTDOWN的状态才允许获取任务，所以例如STOP或者更高的状态下`getTask`就会获取不到线程，从而导致工作线程的退出。

再来会检查当前的线程是否需要淘汰，最后才会获取任务。



工作线程的淘汰的场景如下:

1. 工作线程数直接大于maximumPoolSize
2. 工作线程大于corePoolSize并且获取任务超时过一次
3. 核心线程允许淘汰并且当前线程大于1或者工作队列为空





### 尝试终止 - tryTerminate

该方法在类中很多地方都会调用,比如addWorker失败，worker线程退出等等情况。

相当于一个后置的检查方法，检查线程池应不应该进入TERMINATE状态，在可终止但是仍有线程在运行的情况下，尝试中断空闲线程。

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
                // 替换为TIDYING状态，并且执行terminated方法
                // 这里也可以看出来，TIDYING是一个过渡状态。
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

通过第一个对于线程池状态的判断，我们可以得出以下关于转化为TERMINATED状态的要求:

1. 线程池为STOP状态
2. 线程池为SHUTDOWN状态，但是workerQueue(阻塞队列)必须为空



满足前面两个要求的线程池，调用该方法之后如果工作线程不为0，则尝试关闭空闲线程，

如果工作线程为0，则转化为TERMINATED状态。



 

### 中断空闲线程 - interruptIdleWorkers

该方法用来中断线程池中空闲的线程。

```java
// 入参onlyOne - 只关闭一个线程  
private void interruptIdleWorkers(boolean onlyOne) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                    // 遍历工作线程集合
                    for (Worker w : workers) {
                        Thread t = w.thread;
                        // sInterrupted会检查线程是否已经中断
                        // w.tryLock就是空心啊
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
                        // 如果只关闭一个的话现在就退出
                        if (onlyOne)
                            break;
                    }
            } finally {
                mainLock.unlock();
            }
    }
```

整体的方法逻辑就是遍历整个workers集合，调用`tryLock`方法尝试获取锁，成功就触发中断。



根据源码不难推断**`tryLock`就是验证线程是否空闲的方法**，在调用`interrupt()`方法之前，都会调用Woker的`tryLock()`方法。

结合[Worker的源码](#Worker)发现，`tryLock()`实际就是调用`tryAcqurire`方法，而`tryAcquire()`方法就是尝试CAS把state从0变成1，且只会尝试一是次，所以`tryLock()`只有state=0的时候才会成功，也就顺理成章的推断**state=0即表示线程空闲**。

所以也就验证了上文中提到的，AQS的state属性在这里就表示当前线程是否空闲。



**这里再次强调在线程池中的线程还在执行任务时，是无法被中断的，因为`tryLock`方法会失败。**



### 线程池关闭 - shutdown/shutdownNow

ThreadPoolExecutor中有很多种关闭线程的方式。



以下是强制关闭的方法 `shutdownNow()`:

 ![image-20200925170630479](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200925170630479.png)

该方法通过将线程池的状态置为STOP来关闭线程池，并且会中断所有的线程，最后返回阻塞队列中的任务，但是不包含正在执行的任务。

`interruptWorkers()`方法会遍历调用Worker的`interruptIfStarted()`方法。

 ![image-20200925171236897](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200925171236897.png)

第一个```getState() >= 0```的条件会过滤掉刚创建并没有调用`runWorker()`的线程。

其他线程不需要获取锁强制执行中断方法，可能会影响到正在执行中的任务。

drainQueue()会返回所有阻塞队列中的任务。



以下算是优雅关闭的方法`shutdown()`

 ![image-20200925170649962](https://chenqwwq-img.oss-cn-beijing.aliyuncs.com/img/image-20200925170649962.png)

相同的检查之外，不同的是将状态变为SHUTDOWN。

SHUTDOWN状态下的线程并不会直接关闭而是会继续消费阻塞队列中的任务，此时只会关闭空闲线程，执行中的线程即使你把它中断了，它也会重置中断标记位。


