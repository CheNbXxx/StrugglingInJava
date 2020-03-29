### JMM - JavaMemor	default void contextLoaded(ConfigurableApplicationContext context) {}yModel

  	简单理解,暂不深入。 (〃'▽'〃)

![运行时内存区域](https://github.com/CheNbXxx/StrugglingInJava/blob/master/all_demo/src/main/img/c9ad2bf4-5580-4018-bce4-1b9a71804d9c.png)

---

#### Java 内存区域

- 方法区    `Method Area`
  - **线程共享**区域，主要用于存储**已被虚拟机加载的类信息，常量，静态变量以及及时编译器编译后的代码**。
  - **运行时常量池**
    - 在编译器被确定，并被保存在已编译的`.class`文件中的一些数据，包含类、方法、接口等中的常量，也包含字符串常量。
  - 不需要连续的内存区域。
- 堆    `Java Heap`
  - **线程共享**区域，在虚拟机启动时就创建，是虚拟机中占用内存最大的一块区。
  - 所有的对象都在这里分配内存，所以也是垃圾收集的主要区域，又称为`GC堆`。
  - 不需要连续的内存，也可以动态增加其内存。
- 程序计数器    `Program Counter Register`
  - 属于**线程私有**的数据区域，占内存不多，主要代表**当前线程所执行的字节码行号指示器**。通过改变这个计数器的值来选取下一条需要执行的字节码指令，完成分支，循环 ，跳转，异常处理，线程相关等操作。
  - 如果执行的是`Java`方法，记录字节码的的指令地址，如果执行`Native`，则为空`Undefine`。
- Java虚拟机栈    `Java Virtual Machine Stacks`
  - 属于`线程私有`的数据区域，代表`Java`方法执行的内存模型。
  - 每个方法执行时都会创建一个栈桢来存储方法的变量表、操作数栈、动态链接方法、返回值、返回地址等信息。
  - 生命周期和线程相同。
- 本地方法栈    `Native Method Stacks`
  - 同属于**线程私有**的数据去用，主要和`Native`方法相关。



---

