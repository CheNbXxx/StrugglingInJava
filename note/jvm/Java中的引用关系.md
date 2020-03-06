- 在看`TreadLocal`的源码的时候看到了`ThreadLocalMap`的`Entry`内部类继承了`WeakReference`，只知道和`GC`有关也没深入了解，所以来做个整理吧。

- `Java`中的引用关系从强到弱依次：

  - **强引用**  (Strong Reference)
  - **软引用**  (Soft Reference)
  - **弱引用**  (Weak Reference)
  - **虚引用**  (Phantom Reference)





#### 强引用

- **强引用**是我们日常用的最多的,我们`new`出的都是强引用。

  ```  java
  Object o = new Object
  ```

- 只要有强引用存在，`GC`就永远不会回收该对象，当内存不足时，`JVM`宁可抛出`OOM`也不会回收引用指向的内容。

- 也可以理解为**强引用会强制对象留在内存空间**



#### 软引用

- 如果一个对象存在**软引用**，那么在内存空间足够时，`GC`就不会对他动手，但如果内存空间不足时，`GC`就会着手清理这些软引用指向的对象。

  ```java
  SoftReference<String> softRef = new SoftReference<>(new String("Hello"));
      // 软引用的声明方式
  ```

-  软引用可以和一个引用队列（ReferenceQueue）联合使用，如果软引用所引用的对象被垃圾回收器回收，Java虚拟机就会把这个软引用加入到与之关联的引用队列中。



#### 弱引用

- **弱引用**的地位就更低了，`GC`只要发现自己管辖的内存区域中有对象只持有弱引用，不管此时的内存使用情况如何，都会回收该对象。

  ```java
   WeakReference<String> softRef = new  WeakReference<>(new String("Hello"));
   System.out.println(softRef.get());
   System.gc();  // 通知GC运行
   System.out.println(softRef.get());
  // 你会发现第二次输出就为null了... String对象被毫无尊严的抛弃了...
  ```

- `ThreadLocal`中作为`ThreadLocalMap`的实际节点`Entry`的父类,持有节点的引用
```java
	// Entry的类声明
	static class Entry extends WeakReference<ThreadLocal<?>>
```
- 将`Entry`设置为弱引用的作用就是消除内存泄露的风险,若在JVM中仅有`ThreadLocalMap`持有着`Entry`的弱引用,而无其他的`强引用`则在下次`GC`时会被直接回收.



#### 虚引用

-   **虚引用**就更没有尊严了，顾名思义，虚引用就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。
