### Semaphore

- Java并发工具之一，用于控制同时访问某个公共资源的线程数，保证资源的合理使用。

- 和`CountDownLatch`一样在内部实现了一个通过集成`AQS`实现了一个`Sync`的内部类(使用`AQS`中的**共享模式**保证资源在多个线程共享时并发的正确性).



---

#### 内部类

```java
  abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1192457210091910933L;
		// state在Semaphore中解释为permits(许可证)
        Sync(int permits) {
            setState(permits);
        }
        final int getPermits() {
            return getState();
        }

      	// 非公平模式下的锁获取 
      	// param：`acquires`表示需要获取的`permits`数目
      	// return：负数表示获取失败
        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                // state即为最大允许获取数目
                int available = getState();
                int remaining = available - acquires;
                // 短路特性
                // `remaining`为负直接返回负数,表示获取失败
                // `remaining`为正，则`CAS`置换，返回剩余，获取成功
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }

      	// 共享模式下的释放操作
      	// return: true表示释放成功
        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                if (compareAndSetState(current, next))
                    return true;
            }
        }

      	// 减少许可证数量
        // param: `reductions`减少的数量
        final void reducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // underflow
                    throw new Error("Permit count underflow");
                if (compareAndSetState(current, next))
                    return;
            }
        }

      	// 这个狠...直接清空所有的`permits`
      	// return: 返回当前的`permits`数目
        final int drainPermits() {
            for (;;) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**
     * NonFair version
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -2694183684443567898L;

        NonfairSync(int permits) {
            super(permits);
        }
		// 非公平模式下的获取操作,直接调用的`Sync`的`nonfairTryAcquireShared`
        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

	/**
     * Fair version
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = 2014338818796000944L;

        FairSync(int permits) {
            super(permits);
        }
		// 公平模式下的获取就比非公平模式多一个`检查前驱操作`
        protected int tryAcquireShared(int acquires) {
            for (;;) {
                // `hasQueuedPredecessors`就是`AQS`中检查前驱的方法
                if (hasQueuedPredecessors())
                    return -1;
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }
    }
```



#### 构造函数

```java
// 默认为非公平模式  
public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }

 public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }
```





#### 资源（`permits`）获取方法

- 获取`permits`的方法有点多...
- 因为调用的`AQS`方法，所以获取`permits`的具体流程就是`AQS`中获取共享锁的流程(尝试获取，失败则进入同步队列，等待唤醒)

```java
// 	获取失败进入等待队列，中断则抛错
public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }
// 获取失败进入等待队列，不可中断
public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }
// 获取失败直接返回false，不进入队列
public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }
// 尝试获取,带超时时间,时间到返回false
public boolean tryAcquire(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }
// ↑↑↑ 上面都是默认获取一个`permits`   
// 						全军出击(((ꎤ'ω')و三 ꎤ'ω')-o≡
// ↓↓↓ 下面可以指定获取`permits`的个数
 public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }
 public void acquireUninterruptibly(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireShared(permits);
    }
 public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.nonfairTryAcquireShared(permits) >= 0;
    }
 public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }


```



#### 资源(`permits`)释放方法

```java
 // 释放一份`permits`的方法
public void release() {
        sync.releaseShared(1);
    }

// param: `permits`表示释放的数目
public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

// ↓↓↓ 下面的方法在`AQS`中看过...就当复习一下
// 共享节点的唤醒在后继节点唤醒后

// `AQS`方法
 public final boolean releaseShared(int arg) {
     	// 尝试释放方法,`CAS`操作成功返回·true· 
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
 }
// `AQS`方法
private void doReleaseShared() {
        for (;;) {
            Node h = head;
            // 头节点不为空，且不为尾节点(队列还有其他线程)
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                // `SIGNAL`状态时唤醒后续节点
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    // 唤醒后继节点后，后继节点继续执行`doAcquireShared`中的循环
                    // `doAcquireShared`中的`setHeadAndPropagate`会改变head
                    unparkSuccessor(h);
                }
                // 确保传播
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }

  // `AQS`方法
 private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
     	// 不为取消状态的都变为初始状态
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);
        Node s = node.next;
     	// 后继节点不为空 或者 后继节点状态为取消时
        if (s == null || s.waitStatus > 0) {
            // 后往前选择最靠近的不为初始和取消状态的节点
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
     	// 不为空就唤醒
        if (s != null)
            LockSupport.unpark(s.thread);
    }
```



