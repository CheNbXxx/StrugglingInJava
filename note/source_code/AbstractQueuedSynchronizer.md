## 		AbstractQueuedSynchronized源码阅读 - 同步队列



* `AQS`是用于构造锁和基本同步器的基础框架，是JUC中大部分同步工具实现的基础。

* `AQS`内部维护的两种队列结构用来存放等待线程:
  1. 由`AQS`自身的成员变量`head`和`tail`维护,一个**以内部类`Node`为元素的`CLH队列`**，可以称之为**同步队列**,利用`CLH队列`的特性可以完美解决饥饿问题.

  2. 内部类`ConditionObject`实现一个等待条件的双向队列，称之为**条件队列**.由`ConditionObject`中的`firstWaiter`和`lastWaiter`维护.

* `AQS`提供**独占和共享**各三种上锁模式:
  1. 普通模式 - `acquire`
  2. 可中断模式 - `acquireInterruptibly`
  3. 带超时时间的模式 - `tryAcquireNanos`

* 以下主要讲解的是`CLH同步队列`的实现部分.

---

### 1） 成员变量

```java
// 底层FIFO队列的头节点	         
private transient volatile Node head;    
// 尾节点
private transient volatile Node tail;           
```

- `head`,`tail`用来维护AQS内部的CLH队列.



```
private volatile int state;       
```

- **当前同步状态**,通过`volatile`保证了线程间的可见性。

- `state`可以说是其中最为关键的一个成员变量,在`JUC`不同的实现类中有不同的体现:

  - **ReentrantLock - 存储重入数**
  - **Semaphore - 许可数**
  - **CountDownLatch - 需要调用countDown的次数**
  - **Future - 存储当前任务的执行状态**

  

### 2）  内部类

#### Node - 静态内部类

```java
    static final class Node {
            // 共享模式
            static final Node SHARED = new Node();
            // 独占模式
            static final Node EXCLUSIVE = null;        
            // CANCELLED  表示当前的线程被取消
            // SIGNAL     表示当前节点的后继节点(包含的线程)需要运行
            // CONDITION  表示当前节点在等待condition
            // PROPAGATE  表示当前场景下后续的acquireShared能够得以执行
            // 值为0，表示当前节点在sync队列中，等待着获取锁
        	// 后面会有许多状态大于0的判断,就是判断是否取消
            static final int CANCELLED =  1; 
            static final int SIGNAL    = -1;
            static final int CONDITION = -2;
            static final int PROPAGATE = -3;        
            // 结点状态
            volatile int waitStatus;        
            // 前驱结点
            volatile Node prev;    
            // 后继结点
            volatile Node next;        
            // 结点所对应的线程
            volatile Thread thread;        
            // 下一个等待者
            Node nextWaiter;
            // 结点是否在共享模式下等待
            final boolean isShared() {
                return nextWaiter == SHARED;
            }
            // 获取前驱结点，若前驱结点为空，抛出异常
            final Node predecessor() throws NullPointerException {
                // 保存前驱结点
                Node p = prev; 
                if (p == null) // 前驱结点为空，抛出异常
                    throw new NullPointerException();
                else // 前驱结点不为空，返回
                    return p;
            }
            // 无参构造函数
            Node() {    // Used to establish initial head or SHARED marker
            }
            // 构造函数
             Node(Thread thread, Node mode) {    // Used by addWaiter
                this.nextWaiter = mode;
                this.thread = thread;
            }
            // 构造函数
            Node(Thread thread, int waitStatus) { // Used by Condition
                this.waitStatus = waitStatus;
                this.thread = thread;
            }
        }
```

- **以`Node`实例作为共享或者独占模式的表示。（Node.SHARED | Node.EXCLUSIVE）**
- 同步状态具体如下表所示：

| 状态 | 含义 | 数值 |
| :----:  | :----:   | :----: |
| CANCELLED  | 等待队列中的线程被中断或者超时,会变为取消状态 | 1 |
| SIGNAL     | 表示该节点的后继节点等待唤醒，在完成该节点后会唤醒后继节点|-1|
| CONDITION  | 该节点位于条件等待队列,当其他线程调用了`condition.signal()`方法后会被唤醒进入同步队列 | -2|
| PROPAGATE  | 共享模式中，该状态的节点处于Runnable状态            | -3 |
| 初始状态    | 初始化状态                                          | 0 |

** 表格第二行的短横少加一个竟然就不能显示为表格  (╬◣д◢)**

---

### 3） 锁资源的获取/释放逻辑

####  独占锁的获取

##### 1. 上层的锁获取方法 - acquire & tryAcquire



```java
    /**
      *  尝试以独占形式获取锁
      *  在ReentrantLock中是锁已经被占用的情况下调用
      */
    public final void acquire(int arg) {
        // tryAcquire仅为模板函数,需要在子类中实现获取锁的逻辑
        // 获取锁失败之后才会执行&&后面的代码
        if (!tryAcquire(arg) &&	   
            // addWaiter中以独占模式入CLH队列
            // acquireQueued是在入队列之后挂起线程
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();	
    }

    /**
     * 受保护的类,tryAcquire顾名思义为尝试获取资源， true表示获取成功,false为获取失败
     * 在AbstractQueuedSunchronizer中仅仅是抛出错误，具体的获取逻辑在子类中实现
     * 类似于模板模式
     */
    protected boolean tryAcquire(int arg) {
            throw new UnsupportedOperationException();
    }
```

- `JDK`的源码中经常会用到逻辑运算的**短路特性**。
- 加锁的逻辑时可以直接从`acquire`中的if判断中看出个大概:
  1. `tryAcquire` - 尝试获取锁资源
  2. `addWaiter` - 加入到等待队列
  3. `acquireQueued` - 已经加入队列后的等待
- `tryAcquire`采用的`模板模式`在源码中也比较常见，除了`AQS`之外还有在`LinkedHashMap`中，子类通过对父类方法的不同实现进而在父类中有不同的代码逻辑。
- `tryAcquire`中会尝试获取资源，若果失败则进入`addWaiter`方法。



##### 2. 入队列方法

###### addWaiter  -  争锁失败节点的首次入队列

```java
	/**
      * 该方法用于以指定模式添加当前线程到等待队列
      * params： mode -> 共享还是私有模式
      * return:  返回实际入队节点
      */
     private Node addWaiter(Node mode) {
            // 创建node实例
            Node node = new Node(Thread.currentThread(), mode);
            // Try the fast path of enq; backup to full enq on failure
            // 获得尾节点
            Node pred = tail;
            // 如果尾节点不为空，在此处尝试入队列
            if (pred != null) {
                // 当前节点的上一个节点指向尾节点
                node.prev = pred;
                // CAS操作，将node置为尾节点，同时只能有一个线程操作成功，其他失败的进入enq方法
                if (compareAndSetTail(pred, node)) {
                    // pred下一个节点指向当前节点
                    pred.next = node;
                    return node;	// 直接返回就不调用enq方法
                }
            }
            // 此处调用enq是在首次入队失败之后
            enq(node);
            return node;
        }
```

- 整个方法**仅做一次入队列尝试**，失败则会调用`enq`完成后续操作。
- `Node`节点的创建也是在`addWaiter`中。
- `addWaiter`的**作用可能就是减少动作指令**，在失败之后才会进入`enq`中的无限循环.



###### enq  -  CAS入队列 || 新建CLH队列

```java
	 /**
      *  return： 返回入队节点的前驱节点
      */
     private Node enq(final Node node) {
            // 无限循环确保成功入队列
            for (;;) {
                // 获取尾部节点
                Node t = tail;
                // 尾节点为空时表示队列未初始化，必须创建!!
                if (t == null) { // Must initialize
                    // 此处注意!!!是以一个空的节点作为头节点
                    if (compareAndSetHead(new Node()))
                        // 首尾一致,此时CLH中只有一个元素
                        tail = head;
                } else {    // 头节点不为空时
                	// 挂在尾节点的后面
                    node.prev = t;
                    // CAS操作将当前节点置换为尾节点
                    if (compareAndSetTail(t, node)) {
                        // 尾节点的下一个节点为当前节点
                        t.next = node;
                        return t;	// 在成功后返回入队节点，否则为无限循环
                    }
                }
            }
        }
```

- 将node入队的逻辑方法，**在第一次入队失败或者队列未初始化时调用，有自旋操作保证入队列尾成功**.



##### 3. 挂起线程方法

###### acquireQueued  -  挂起线程的主方法

```java
final boolean acquireQueued(final Node node, int arg) {
            // 失败标识
            boolean failed = true;
            try {
                // 是否中断标志
                boolean interrupted = false;
                for (;;) {              // 自旋 无限循环 ！
                    // 获取前驱节点
                    final Node p = node.predecessor();
                    // 重点 ！！！ 在前驱节点变为头节点时，才再次尝试获取锁.
                    if (p == head && tryAcquire(arg)) {
                        // 将该节点置为头结点 表示成功获取到锁
                        setHead(node);  
                        // 头结点的next为空 帮助GC回收	
                        p.next = null; // help GC
                        failed = false;
                        // 获取成功时返回 
                        return interrupted;  
                    }
                    //  !!! 重点 获取失败之后的准备操作，将前驱节点变为SIGNAL等
                    if (shouldParkAfterFailedAcquire(p, node) &&
                        // 调用park***方法后会park当前线程，等待unpark
                        parkAndCheckInterrupt()) 
                        // 在前驱状态为SIGNAL，且线程暂停成功之后，置true
                        interrupted = true; 
                }
            } finally {    
                if (failed)
                    cancelAcquire(node);
            }
        }	
```

- 挂起入参节点线程,此时经过`addWaiter`和`enq`已经确保当前线程节点入队列.
- 打破无限循环的关键还是先尝试获取锁,**只有在有当前节点的前驱节点为头节点的时候才会尝试获取锁**.



###### shouldParkAfterFailedAcquire  -  线程挂起前的准备逻辑

```java
     /**
	  * params：分别为前驱节点，当前节点
	  * returns: 是否准备完成,前驱节点状态为SIGNAL表示准备完成
	  */
     private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
            int ws = pred.waitStatus;   // 获取前驱节点的状态
            // 前驱节点的状态为Signal
            if (ws == Node.SIGNAL)  
                return true;	
            // 如前面提示>0就表示节点是取消
         	if (ws > 0) {
                // 循环遍历找到不是取消状态的节点,取消表示前驱不在尝试获取锁
                // 从后往前遍历找到第一个不是取消的锁,并牌子啊他的后面
                do {
                    node.prev = pred = pred.prev;
                } while (pred.waitStatus > 0);
                // 找到前一个不在取消的锁并排在他的后面
                pred.next = node;
            } else {
                // 如果前驱状态不是取消，则改变前驱状态为Signal，即表示本节点需要锁
                // 此处将前驱节点置为SIGNAL,再经过acquireQueued再一次进入时,
                // 会在第一个判断直接返回true
                compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
            }
            return false;
     }
```

- 在线程挂起前的准备逻辑,包括:
  1. 将前驱节点的节点状态变为`Node.SIGNAL`.
  2. 清除状态为`Node.CANCELLED`的无用节点.
- 如果前驱状态为`SIGNAL`则直接成功,否则需要两次循环才能成功.



###### parkAndCheckInterrupt  -  直接挂起线程的方法

```java
      /**
        *  挂起线程,并在被唤醒后返回中断状况
        */
     private final boolean parkAndCheckInterrupt() {
            LockSupport.park(this);		 //调用park()使线程进入waiting状态
            return Thread.interrupted();
        }
```

- 可以看到`AQS`挂起线程使用的是`LockSupport`



###### cancelAcquire - 取消获取

```java
	  /**
		*   取消获取锁,acquireQueued方法的fianlly内进入
		*      保证不会是因为错误无限循环 浪费系统资源
		*  params：获取锁失败的节点
		*/
   private void cancelAcquire(Node node) {
        // Ignore if node doesn't exist
        if (node == null)
            return;
        node.thread = null;
        // Skip cancelled predecessors
        Node pred = node.prev;
        while (pred.waitStatus > 0)			// 前驱节点状态为取消时，直接删除该节点
            node.prev = pred = pred.prev;
        Node predNext = pred.next;	// 获取后继节点.. 是被跳过的节点
        // 修改前驱节点状态为取消
        node.waitStatus = Node.CANCELLED;	
        // node为尾节点，将该节点前驱变为tail，并将前驱节点的next置空
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            int ws;
            // 不为头结点 执行下面判断CANCELLED
            if (pred != head &&	
                // 赋值并判断 不为SIGNAL进行下面判断
                ((ws = pred.waitStatus) == Node.SIGNAL ||	
                 // 非取消状态就将状态改为SIGNAL，使用CAS保证并发		
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&	
                 // 此判断会将前驱节点全变为SIGNAL状态
                pred.thread != null) {  
                 // 当前节点的后继节点
                Node next = node.next;   
                // 后继不为空，且状态不为取消
                if (next != null && next.waitStatus <= 0)	
                    // 在同步队列中删除当前节点
                    compareAndSetNext(pred, predNext, next);
            } else {
                // 唤醒方法在释放流程中
                unparkSuccessor(node);		
            }

            node.next = node; // help GC
        }
    }
```

- 在获取锁未成功的情况下意外退出时调用该方法,意外退出之后的后续操作.包括:
  1. 将前驱节点状态变为`CANCELLED`
  2. 从同步队列中删除当前节点
  3. 保证后继节点可靠



##### 4. 整个获取流程梳理及关键点

> 整个获取独占锁的的流程
> 1. 首先调用`tryAcquire`方法尝试获取锁
> 2. 获取锁失败之后,先调用`addWaiter`方法以独占方式将锁加入到等待队列末尾
>    1. `addWaiter`中会首次尝试入队,失败后调用`enq`
>    2. `enq`内自旋，负责初始化队列以及保证线程入队尾
> 3. `acquireQueued`负责获取锁以及在失败之后挂起线程
>
>    1. 自旋判断前驱节点,为头节点时才会尝试获取资源，失败时候进入挂起流程
>    2. `shouldParkAfterFailedAcquire`方法是**线程挂起前的准备操作**，将前驱节点状态变为`SIGNAL`,表示后继节点等待锁资源.
>    3. `parkAndCheckInterrupt`调用**LockSupport类**挂起当前线程,并在唤醒后返回中断信息.
>    4. `acquireQueued`的finally代码块保证了异常或中断退出时的资源释放,该代码会调用`cancelAcquire`出队并释放后继节点

 - `acquireQueued`中的获取锁失败之后的挂起流程
    1. 第一次循环时前驱不为头结点或者尝试获取锁失败,此时会进入`shouldParkAfterFailedAcquire`方法,将前驱节点状态变为`SIGNAL`,并返回false,开始第二次循环.
    2. 第二次获取失败之后,也是进入`shouldParkAfterFailedAcquire`方法,但因为第一次时前驱节点已经被为`SIGNAL`状态,此次直接返回true,之后就进入`parkAndCheckInterrupt`方法挂起当前线程.

- 获取过程中`CLH队列`中,节点状态是为后继节点的状态.

   -  当前节点状态为`SIGNAL`,表示后继节点需要锁资源
   -  当前节点状态为`CANCELLED`,表示后继节点不再需要锁资源.




#### 独占锁的释放

- 相对于锁的获取来说,释放锁简单许多.

##### release  -  释放锁的上层方法

- release()方法是独占模式下释放锁的入口.

> release的流程
> 1. `tryRelease`尝试释放资源,若释放成功返回true
> 2. 释放成功之后调用`unparkSuccessor`唤醒队列中下一个等待线程
- **tryRelease()**的返回值可以分为三种情况,分别是**负值表示获取失败,0表示获取成功但无剩余资源,正值表示获取成功且有剩余资源.**

```java
    public final boolean release(int arg) {
        // 尝试释放锁 失败进入if代码
        if (tryRelease(arg)) {
            // 获取头节点
            Node h = head;      
            // 如果头节点不为空 且想头节点的状态不是等待获取锁
            if (h != null && h.waitStatus != 0)
                // 唤醒队列中下一个等待的线程
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
    
    /**
     * 尝试释放共享资源的锁，具体的逻辑实现由子类完成 
     * return:true为已经释放
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }
       /**
        *  唤醒等待队列中下一个线程  
        * 		入参:唤醒节点的前驱节点或更前
        */
     private void unparkSuccessor(Node node) {
            ws = node.waitStatus;
            // 判断并置0当前线程状态(取消状态不论)
            if (ws < 0)
                compareAndSetWaitStatus(node, ws, 0);
            // 获取后继节点
            Node s = node.next;
            // 1. 后继节点为空 或者 后继节点的状态为取消 
            if (s == null || s.waitStatus > 0) {
                s = null;
                // 从队尾开始向前找第一个有效节点
                for (Node t = tail; t != null && t != node; t = t.prev)
                    // 判断是否是有效节点
                    if (t.waitStatus <= 0)    
                        s = t;
            }
         	// 2. 不为空时直接唤醒后继节点
            if (s != null)
                LockSupport.unpark(s.thread);
        }
    
```



---



#### 共享锁相关

- 共享锁总体上和独占锁没有多大区别所以简略看了一下.

##### `acquireShared`   该方法以共享的形式获取锁
```java
    /**
      *  以共享的形式获取锁
      */
    public final void acquireShared(int arg) {
        // 尝试获取锁
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }
    /**
      *  尝试获取锁的方法,具体实现在子类
      *  因为在acquireShared中的判断,所以对成功或者失败的返回值有限制
      *  负数代表获取失败，0表示获取成功,但没有剩余资源,正数代表获取成功还有资源，需要唤醒共享节点
      */
    protected int tryAcquireShared(int arg) {
            throw new UnsupportedOperationException();
    }
    /**
      *  共享模式下使当前线程进入等待队列(进入队尾) 
      */
    private void doAcquireShared(int arg) {
            // 创建共享模式的node实例并添加到队列末尾
        	// 独占锁中一样,addWaiter会先尝试入队列一次,失败后enq自旋入
        	// 队列也会在enq中创建
            final Node node = addWaiter(Node.SHARED);
            // 是否成功标识
            boolean failed = true;
            try {
                // 是否被中断标识
                boolean interrupted = false;
                // 自旋操作
                for (;;) {      
                    // 获得前驱节点,为空抛NPE
                    final Node p = node.predecessor();
                    // 如果node的前驱节点是head,node处于第二则有机会获得资源
                    if (p == head) {    
                        // 尝试获取资源
                        int r = tryAcquireShared(arg);
                        // 正数代表资源还有剩余，
                        if (r >= 0) {   
                    // 重点方法,共享模式的精髓 在自身获取到锁之后设置传播 唤醒后继共享节点
                            setHeadAndPropagate(node, r);		
                            p.next = null; // help GC
                            if (interrupted)
                                selfInterrupt();
                            failed = false;
                            return;
                        }
                    }
                   	// 尝试获取锁失败之后的挂起逻辑和独占锁一样
                    if (shouldParkAfterFailedAcquire(p, node) &&
                        parkAndCheckInterrupt())
                        interrupted = true;
                }
            // 退出循环时没有获取到锁，则置为取消状态
            } finally {     
                if (failed)
                    cancelAcquire(node);
            }
        }
        /**
          *    设置头节点并唤醒可以共享的线程
          */
         private void setHeadAndPropagate(Node node, int propagate) {
                Node h = head; // Record old head for check below
                setHead(node);   
                // 操作太骚 我看不懂
                if (propagate > 0 || h == null || h.waitStatus < 0 ||
                    (h = head) == null || h.waitStatus < 0) {
                    Node s = node.next;
                    if (s == null || s.isShared())
                        doReleaseShared();
                }
            }
```

- 共享形式和独占形式在代码上大同小异,**在共享模式的获取锁完成之后会尝试唤醒别的线程**,主要还是四种线程状态的判断。

---

## AbstractOwnableSynchonizer

```java 
 /**
   * A synchronizer that may be exclusively owned by a thread.  a
   * This class provides a basis for creating locks and related synchronizers 
   * that may entail a notion of ownership. 
   */
```
* 该类主要定义了线程独占的方式拥有的同步器，提供了创建锁和相关同步器的基础，并且可能会涉及到所有权的概念。

```java
    public abstract class AbstractOwnableSynchronizer
            implements java.io.Serializable {
        private transient Thread exclusiveOwnerThread;      // 独占线程
        // 设置当前拥有独占访问权的线程
        protected final void setExclusiveOwnerThread(Thread thread) {
                exclusiveOwnerThread = thread;
        }
        // 获取当前拥有独占访问权的线程
        protected final Thread getExclusiveOwnerThread() {
                return exclusiveOwnerThread;
        }
    }
```
* 由源码可见在AbstractOwnableSynchronizer中并不提供对该独占线程的管理

---

## AQS相关问题

### CLH队列 & CLH锁
- `CLH`锁就是一种以链表为基础的,可扩展的高性能自旋锁,线程以本地变量为基准自旋,并轮询前驱节点的状态,以确定自旋的结束时间.所谓的`前驱节点`就是队列中在当前节点之前的线程节点.
- `AQS`本身维护的链表其实就是一个`CLH队列`,以`Node`内部类构造节点.
- 推荐阅读:[CLH队列相关博客](https://coderbee.net/index.php/concurrent/20131115/577)


