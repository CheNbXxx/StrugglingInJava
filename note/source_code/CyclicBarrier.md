### 	CyclicBarrier

- `CyclicBarrier`是一个可以重用的同步辅助类，允许一组线程相互等待，在达到一个共同点在继续执行。
- 当一个线程到达集合点时，它将调用await()方法等待其它的线程。线程调用await()方法后，CyclicBarrier将阻塞这个线程并将它置入休眠状态等待其它线程的到来。等最后一个线程调用await()方法时，CyclicBarrier将唤醒所有等待的线程然后这些线程将继续执行。CyclicBarrier可以传入另一个Runnable对象作为初始化参数。当所有的线程都到达集合点后，CyclicBarrier类将Runnable对象作为线程执行。 

---

#### 成员变量

 ```java
  /** The lock for guarding barrier entry */
// 在CB底层终归还是使用ReentrantLock保证并发的正确性
    private final ReentrantLock lock = new ReentrantLock();	 
    /** Condition to wait on until tripped */
// 等待的条件，CB中调用该对象的wait方法阻塞线程
    private final Condition trip = lock.newCondition();		
    /** The number of parties */
// 参与等待的线程数目
    private final int parties;					         
    /* The command to run when tripped */
// 再开闸后需要调用的进程
    private final Runnable barrierCommand;				
    /** The current generation */
// Generation的break用来记录当前的Barrier是否被打破
    private Generation generation = new Generation();		

    /**
     * Number of parties still waiting. Counts down from parties to 0
     * on each generation.  It is reset to parties on each new
     * generation or when broken.
     */
    private int count;			// 继续还需调用await方法的线程数

 private static class Generation {
        boolean broken = false;
    }
 ```

 

---

#### 构造函数

- 简单地 随便看看就好 (￣▽￣)~*

```java
public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

public CyclicBarrier(int parties) {
        this(parties, null);
    }
```



---

#### 核心方法

```java
// 执行await方法时候，线程被阻塞 并不释放资源 
public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }

  // 指定超时时间的await()方法
   public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }	

 private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        // 获取重入锁实例
        final ReentrantLock lock = this.lock;				
       	// 上锁操作
        lock.lock();									  
        try {
            // 获取当前代,final保证获取之后的不可变
            final Generation g = generation;	
            // 检查栅栏是否被打破
            if (g.broken)			
                throw new BrokenBarrierException();
            
			// 线程是否中断，打破栅栏并抛出异常
            if (Thread.interrupted()) {				
                breakBarrier();
                throw new InterruptedException();
            }

            // 此处可见count记录的是已经在等待的线程数目
            int index = --count;				
            // index=0表示等待线程数达到预先设定的数目
            if (index == 0) {  // tripped	
                //  线程执行标识(待商榷)
                boolean ranAction = false; 	
                try {
                    // 在栅栏被打破之后的执行线程
                    final Runnable command = barrierCommand;		
                    if (command != null)
                        // 不为空就执行
                        command.run();					
                    // 设置标识
                    ranAction = true;					
                    // 初始化当前栅栏的各种状态
                    nextGeneration();					
                    return 0;
                } finally {
                    if (!ranAction)
                        breakBarrier();				// 打破栅栏
                }
            }

            // loop until tripped, broken, interrupted, or timed out	
            // 自旋操作，无限循环
            for (;;) {					
                try {
                    // 没有设置超时时间
                    if (!timed)
                        // 调用Condition的await方法
                        trip.await();	
                    // 设置了超时时间，并且时间大于0
                    else if (nanos > 0L)	
                        // 设置有超时的等待
                        nanos = trip.awaitNanos(nanos);	
                    // 注意此处捕获的是中断异常
                } catch (InterruptedException ie) {	
                    // 捕获到中断异常时，栅栏未被打破
                    if (g == generation && ! g.broken) {		
                        // 直接打破栅栏，防止死锁
                        breakBarrier();				
                        throw ie;
                    } else {						
                        // 栅栏已经被打破
                        // We're about to finish waiting even if we had not
                        // been interrupted, so this interrupt is deemed to
                        // "belong" to subsequent execution.
                        // 中断当前线程
                        Thread.currentThread().interrupt();		
                    }
                }

                if (g.broken)					// 如果运行到此步发现栅栏已经被打破，抛异常
                    throw new BrokenBarrierException();

                if (g != generation)
                    return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }

  // 初始化栅栏的各类参数，重新使用
  private void nextGeneration() {
        // signal completion of last generation
        trip.signalAll();				// 唤醒所有等待线程，
        // set up next generation
        count = parties;				// 重置需要wait的线程数目
        generation = new Generation();	  // 重置栅栏的状态
    }

 // 打破栅栏的方法，generation未初始化
  private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }
```



---

#### 功能的逻辑流程

```
op1=>operation: CyclicBarrier.await()
op2=>operation: CyclicBarrier.dowait()	核心的阻塞方法，不释放锁
op3=>operation: ReentrantLock.lock() 走一遍加锁的流程，保证dowait方法中并发的安全性
op4=>operation: --count是否等于0（等于0表示等待的线程已达到parties），执行预定的收尾线程，并重新初始化cb对象属性
cond1=>condition: if(!timed) 是否有超时限制
op6=>operation: trip.await()   使用当前cb对象的条件，阻塞线程
cond2=>condition: if(nanos>0L) 等待时间是否大于0
op7=>operation: trip.awaitNanos() 带超时的阻塞


op1->op2->op3->op4->cond1
cond1(no)->op6
cond1(yes)->cond2
cond2(yes)->op7
cond2(no)->cond1

```

- ლ(ಠ益ಠლ).  这个流程图也太他妈复杂了。。只能稍微弄点了，github的竟然不支持md的流程图 直接贴代码了。。