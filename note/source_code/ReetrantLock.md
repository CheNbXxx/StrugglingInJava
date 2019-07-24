### ReetrantLock

- `ReentrantLock`是一种无阻塞可重入锁，底层借助`AbstractQueueSynchronizer`实现两个等待队列
- **要理解ReetrantLock必须先理解广义的重入锁**.
- [究竟什么是可重入锁]("https://blog.csdn.net/rickiyeat/article/details/78314451")

---

####  内部类

##### Sync  

- ***注意：使用AQS的state表示锁的重入数***

```java
abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * Performs {@link Lock#lock}. The main reason for subclassing
         * is to allow fast path for nonfair version.
         */
        abstract void lock();

        /**
         * Performs non-fair tryLock.  tryAcquire is implemented in
         * subclasses, but both need nonfair try for trylock method.
         */
        final boolean nonfairTryAcquire(int acquires) {   
            final Thread current = Thread.currentThread();		  // 获取当前线程
            int c = getState();									// 获取当前state
            if (c == 0) {									    // 为0表示无线程持有锁
                if (compareAndSetState(0, acquires)) {		// 尝试获取锁
                    setExclusiveOwnerThread(current);		// 设置独占线程
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) { // 获取当前独占线程
                int nextc = c + acquires;
                if (nextc < 0) // overflow   // 过多重入导致数值溢出
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);	// 更新重入数
                return true;
            }
            return false;
        }

    	 //    尝试释放资源
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;  			
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {						// 若c为0表示完全释放
                free = true;
                setExclusiveOwnerThread(null);   // 则将当前独占线程置为空
            }
            setState(c);
            return free;					
        }
    }
```





#####  NonfairSync

```java
    /**
     * Sync object for non-fair locks
     */
    static final class NonfairSync extends Sync {		// 继承Sync
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            if (compareAndSetState(0, 1))		// 尝试立即获取
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);		// 失败之后调用，进入等待队列并自旋获取资源
        }

        // 此处实现了tryAcquire方法，是在AQS的acquire()中最先调用的方法，
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }
```



##### FairSync

```java
static final class FairSync extends Sync {		// 继承Sync
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        acquire(1);
    }

    /**
     * Fair version of tryAcquire.  Don't grant access unless
     * recursive call or no waiters or is first.
     */
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) { // 平锁在发现资源可获取时，还会检查在等待队列中是否有等待更久的线程
            if (!hasQueuedPredecessors() &&		// hasQueuedPredecessors()未查询是否有前驱Node
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current); // 设置独占锁
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {  // 当前线程是否独占,继续加重入值
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);		// 增加重入数
            return true;
        }
        // 在锁被占用，且自身不是独占锁的时候返回false
        return false;
    }
}
```

---

#### 方法

##### 构造方法

```java
  /**
     * Creates an instance of {@code ReentrantLock}.
     * This is equivalent to using {@code ReentrantLock(false)}.
     */
    public ReentrantLock() {		
        sync = new NonfairSync();   // ReentrantLock默认采用非公平锁
    }

    /**
     * Creates an instance of {@code ReentrantLock} with the
     * given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public ReentrantLock(boolean fair) {  // 含参构造，true表示公平锁，false表示非公平锁
        sync = fair ? new FairSync() : new NonfairSync();
    }
```

- `ReentrantLock`好像没有其他关键的方法了..

- `Lock`接口的实现方法基本通过Sync引用对象调用方法实现,Sync的引用指向NonfairSync或者FairSync的实例对象... 骚操作，技术活 (；д；)



##### 常用方法、

- getHoldCount();			  获取当前线程的重入数
- isLocked();				  是否被上锁
- isFair();	           		          是否公平
- hasQueuedThreads();		   是否有线程在等待
- getQueueLength();		    获取正在等待当前锁的线程个数
- hasWaiters(Condition);	    获取正在等待当前条件的线程
- ...其他方法也差不多 字面意思基本能理解 ヾ(=･ω･=)o



---



流程图太难画了!!!∑(ﾟДﾟノ)ノ  [【JUC】JDK1.8源码分析之ReentrantLock（三）](https://www.cnblogs.com/leesf456/p/5383609.html)