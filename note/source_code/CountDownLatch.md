### CountDownLatch

- 简单来说`CountDownLatch`就是一个线程计数器,让调用`await()`线程等待,完成任务的线程调用`countDown()`方法，当完成的线程达到预先设置的数量后，唤醒原先使用`await()`阻塞的线程。

---



#### 成员变量

- `CountDownLatch`的成员变量只有一个`Sync`用来保证线程的同步，使用的是`AQS`中的共享模式。
- 因为`wait()`方法可能作用于多个进程，所以这里只能使`共享模式`!!!

---



#### 内部类

- `Sync`内部类继承`AbstractQueuedSynchronizer`。

- `AbstractQueuedSynchronizer`中的`state`属性在该类表示**调用的线程数**

  ```java
   private static final class Sync extends AbstractQueuedSynchronizer {
          private static final long serialVersionUID = 4982264981922014374L;
  		// 此处可以看出state在CountDownLatch中的具体作用
          Sync(int count) {
              setState(count);
          }
          int getCount() {
              return getState();
          }
  	    // 查看能否获取资源 
          // state等于0是表示可以获取到资源
          protected int tryAcquireShared(int acquires) {
              return (getState() == 0) ? 1 : -1;
          }
  		// 重写释放共享锁的方法
          protected boolean tryReleaseShared(int releases) {
              for (;;) {		// 自旋操作
                  int c = getState();			
                  if (c == 0)
                      // state已经变成0，不需要再进行释放操作
                      return false;// state等于0表示资源可获取，并没有持有锁，(不清楚为什么要返回false)
                  int nextc = c-1;
                  if (compareAndSetState(c, nextc))
                      return nextc == 0;
              }
          }
      }
  ```

---



#### 成员方法

- 构造函数
```java
/**
  *   构造函数，比如传入一个count参数表示要调用countDown()方法的线程数
  */
public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);		// 使用Sync的构造函数传入count作为state
    }
```

- `await()`\`await(long timeout, TimeUnit unit)`
```java
/**
  *   await()方法用于是线程阻塞，等待count=0时唤醒，有带超时时间和不带之分
  */

 public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

 public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())			// 抛出中断异常
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)// 此处调用Sync内部类的方法，当state!=0是成立，即count>0，仍然需要等待线程调用countDown()
            doAcquireSharedInterruptibly(arg);
    }

 private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);// 将当前线程加入同步队列，返回的是入栈的节点
        boolean failed = true;
        try {
            for (;;) {	// 自旋
                final Node p = node.predecessor();// 获取前驱节点
                if (p == head) { // 当前驱变为头节点的时候尝试获取资源
                    // 调用Sync内部类的方法 tryAcquireShared就是判断state是否等于0
                    int r = tryAcquireShared(arg); 
                    // r>=0时表示state=0
                    // r在此时只有两种:1,-1
                    if (r >= 0) {  // state == 0的时候
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                // 在p!=head或者state!=0时执行以下方法
                // 先将前驱节点的状态变为SIGNAL，若已经是则park当前线程，等待唤醒
                if (shouldParkAfterFailedAcquire(p, node) && // 1、 准备阻塞线程
                    parkAndCheckInterrupt())			   // 2、阻塞线程，等待唤醒
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

// 两个参数分别为已经获得共享锁的node以及返回值剩余的资源数
// 该方法用于设置头结点并传播
 private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);	// 便捷方法设置头结点
   	    // 1、propagate>0 表示有剩余资源，在CountDownLatch中,propagate只可能为1，所以后面的条件都可以不用判断。
    	// 2、 之前的头节点为空
     	// 3、 之前的头节点状态不为取消
     	// 4、当前头节点为空
     	// 5、当前头节点状态不为取消
     	// 满足以上任一条件便进入if语句
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            // countDownLatch中所有节点都是共享的形式添加到队列的
            if (s == null || s.isShared())
                doReleaseShared();	
        }
    }

	// 唤醒方法
   private void doReleaseShared() {
        for (;;) {	// 自旋
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                // 状态为SIGNAL表示后继节点需要唤醒
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);	// 唤醒h的后继节点
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            // 头结点没有变化表示操作完成，跳出循环
            // 头结点发生变化表示有线程获得了锁，重新进入循环
            if (h == head)                   // loop if head changed
                break;
        }
    }

```

- `countDown()`

```java
  public void countDown() {
        sync.releaseShared(1);		// 很直接的调用，资源数为1
    }

public final boolean releaseShared(int arg) {
    	// 只有在state-arg=0时才会返回true
        if (tryReleaseShared(arg)) {
            doReleaseShared();		// 唤醒wait的进程
            return true;
        }
        return false;
    }
```



