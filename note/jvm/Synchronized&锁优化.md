## synchronized & 锁优化

### synchronized

- `synchronized`是`Java`中最常用的并发关键字.可以保证修饰的区域同一时间只有一个线程可以执行.
- `synchronized`可以保证程序的可见性和原子性.<font size="2">(volatile只能保证可见性以及一定程度上的有序性,而无原子性)</font>
- `synchronized`具有可重入性,同个线程可重复获取<font size="2">(获取之后没释放再获取)</font>
- `synchronized`不具备公平性,会导致`饥饿`。
  - `饥饿`就是指线程因为获取不到想要的资源而长时间不能执行。

<!--more-->

- `synchronized`有三种上锁形式,分别会对不同的对象上锁:

  1. 修饰实例方法

     ```java
     // 此时的上锁对象为当前的实例
     public synchronized void syncMethod(){};
     ```

  2. 修饰代码块

     ```java
     public void syncMethod(){
         // 此时上锁为lock对象.也可为class对象 eg: lock.class
         synchronized(lock){}
     }
     ```

  3. 修饰静态方法

     ```java
     // 此时的上锁对象为class对象
     public static synchronized void syncMethod(){};
     ```

  #### synchronized的底层原理

  - `synchronized`根据不同的实现形式有不同的底层实现.
    - **在修饰代码块时使用的是明确的`monitorenter`和`monitorexit`两个指令**
    - **在修饰方法(包括静态方法)时由方法调用指令读取运行时常量池方法中的`ACC_SYNCHRONIZED`隐式实现**<font size="2">(摘自深入理解Java并发之synchronized实现原理,文末有链接)</font>
  - **不论是显示还是隐式的实现,Java对象头中的`Mark Word`和`monitor（监视器）`都是实现`synchronized`的基础.**

  ##### Mark Word

  - `Mark Word`是`Java对象头`结构中除类型指针外的另一部分,用于记录`HashCode`,对象的年龄分代,锁状态标志等运行时数据.
  - 下图中就比较清晰的展示了,不同情况下`Mark Word`的不同结构.

  ![](https://chenbxxx.oss-cn-beijing.aliyuncs.com/markword.jpg)



  ##### Monitor 监视器(管程)

  - **Monitor是一种用来实现同步的工具,`JVM`中每个对象和类在逻辑上都和一个监视器相关联.**
  - `Monitor`的实现还是依赖于操作系统的`Mutex  Lock`(互斥锁)来实现的.

  ![](https://chenbxxx.oss-cn-beijing.aliyuncs.com/java_monitor.png)

  - 上图即为`Monitor`的结构图
    - **Owner**: 表示所有者,初始为NULL,当该`Monitor`**被某线程持有时,该区域负责保存该线程的唯一标识.**
    - **EntryQ**: 关联一个系统的互斥锁(semaphore),用于阻塞获取当前`Monitor`失败的线程.
    - **RcThis**: 标识等待当前`Monitor`的线程的个数.
    - **Nest**: 重入锁计数,Java的监视器模型天然支持锁重入
    - **HashCode**: 关联对象的对象头中的**HashCode值**.
    - **Candidate**: 记录候选线程信息,避免不必要的阻塞和线程唤醒.

### 锁优化<font size="2">(HotSpot)</font>

- `JDK1.6`之前`synchronized`一直为重量级锁,直接使用互斥锁阻塞线程,但由于**在使用该`Monitor.EntryQ`阻塞线程时需要从用户态切换到核心态,由操作系统层面完成操作**,所以会导致并发的效率并不会太高.
- `JDK1.6`之后,`HopSpot`中加入了**偏向锁,自旋锁,自适应自旋锁,轻量级锁等优化**.
- 锁级别从低到高依次是：无锁状态、偏向锁状态、轻量级锁状态和重量级锁状态。**锁可以升级但不能降级。**

### 锁的转换关系

![](https://chenbxxx.oss-cn-beijing.aliyuncs.com/java_synchronized.jpg)

- 我觉得上图已经很好的展示了几个状态之间的转化,就不在赘述了.<font size="1">(估计也讲不好)</font>

#### 偏向锁相关问题

- 2019-3-6 补充

和Cyc群友讨论的时候发现的问题:**如果使用了偏向锁,那么势必会占据MarkWord中HashCode的位置,那么此时的HashCode又保存在哪里？**

在以下的文章中看的了答案,简单来说就是:

- HashCode和偏向线程Id并不会共存,且HashCode的优先级高于偏向线程ID
- 如果处于偏向锁时,计算了HashCode那么锁会直接膨胀为重量级锁.<font size="2">(水平有限,还没有看英文原文,可自行查看)</font>
- 如果存在HashCode,MarkWord即为不可偏向状态.

[理论基础：翻译《Lock optimizations on the HotSpotVM》](https://ccqy66.github.io/2018/03/07/java锁偏向锁/)

[《Lock optimizations on the HotSpotVM》论文原文](http://ds.cs.ut.ee/courses/course-files/lockOptimizationHotSpot.pdf)

### JDK1.6加入的几种锁优化方案

#### 自旋锁 & 自适应自旋锁

- 引入自旋锁是因为,很多时候线程并不会长时间持有锁,此时使用`Metux`阻塞线程没过一会儿又唤醒就显得很麻烦.
- **自旋锁就是一个循环,在等待持有锁的线程释放锁的过程中,不阻塞线程而让线程处于一直循环尝试获取锁的状态,从而避免了线程切换,阻塞的开销.**
- 但是自旋锁也会占用一部分的CPU时间,若一直无限制自旋也会白白浪费CPU资源,所以在此基础之上又引入了**自适应自旋锁**.
- 自适应自旋锁是对自旋锁的优化,**为自旋的次数或者时间设定一个上限,若超过这个上限一般会选择挂起线程或别的操作.**

#### 锁消除

- 锁消除就是**在逃逸分析技术的支持下**,消除非公用资源的上锁步骤,从而提高性能.

```java
public void test(){
    StringBuffer s = new StringBuffer();
    String a1 = "CheN";
    String a2 = "bXxx";
    s.append(a1);
    s.append(a2);
}
```

- 如上面这段代码展示,其中`StringBuffer`类是线程安全的,方法都会有`synchronized`修饰,所以最少也会有偏向锁的机制在发挥作用,但a1和a2的作用域就在test方法中,完全不会逃逸到方法体外,也不会引起线程安全问题,此时甚至偏向锁都显得很没必要.

#### 锁粗化

- 在一段代码中,若同步区域被限制的过小会导致线程频繁的进行锁的释放和获取操作.而此时锁粗化的作用就出来了,**虚拟机探测到该类情况会直接将锁的同步区域扩展到整个操作的外部**,从而消除无谓的锁操作.

```java
for(int i = 0;i < 10;i++){
    // 此时虚拟机会直接将锁的范围扩展到循环之外
    synchronized(this){
      	doSomething();
    }
}
```

### 相关问题

### 相关文章

- [深入理解Java并发之synchronized实现原理](https://blog.csdn.net/javazejian/article/details/72828483)
- [Java并发——关键字synchronized解析](https://juejin.im/post/5b42c2546fb9a04f8751eabc)
