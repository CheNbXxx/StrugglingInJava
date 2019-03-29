### LinkedList源码阅读

![LinkedList](https://chenbxxx.oss-cn-beijing.aliyuncs.com/LinkedList.png)

- LinkedList`原本是`链表`,`JDK1.6`之后继承了`Deque`接口，又可作为`双端队列`.<font size="2">(自己画结构图被丑哭了)</font>.
- `LinkedList`不仅实现了`List`接口还实现了`Deque`，很明显可以看出除`链表`之外还是一个`双端队列`。
- `LinkedList`和`ArrayList`不同，底层并不是数组，还是一个由`Node`内部类实现的`双向链表`，`Node`结构下面说。

#### 成员变量

```java
// 记录链表的大小
transient int size = 0;
// 记录链表的头结点
transient Node<E> first;
// 记录链表的尾节点
transient Node<E> last;

// 参数全部被`transient`修饰，所以序列化是都不会被记录。
```


#### Node 内部类

- `Node`类非常简单，但是是底层链表的基础结构。
- 除了存储数据之外，`Node`还需要记录前驱和后继节点，串联成一个`双向链表`。

```
// Node就是`LinkedList`底层链表的节点。
// item记录具体内容,next表示后继节点，prev为前驱节点
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
```



#### 构造函数

- `LinkedList`的构造函数也非常简单，此处不放代码。
- 一个默认的**无参构造**，一个就是**以集合为参数的构造函数**，后者调用下面的`addAll()`将集合中的数据转移。



#### 关键方法

- 在写了几个方法之后感觉实现没必要，`add/remov/get`等操作，都是通过一些提取的内部操作方法实现的，并不像`ArrayList`里那样，操作都在方法中。

- 直接写内部具体的链表操作方法把。  (oﾟ▽ﾟ)o  

- 因为同时继承了`Duque`和`List`两个集合接口，所以有两套的操作方法，知道具体的方法可以直接看`Java`的API文档。



#### 链表操作方法

 - **linkBefore(E e,Node<E> succ)**

   - 该方法的作用是将`e`作为节点插入到`succ`节点之前。
   - 类似方法还有`linkFirst`和`linkLast`

   ```java
    void linkBefore(E e, Node<E> succ) {
           // assert succ != null;	 assert语法已经out了
        	// 获取前驱节点
           final Node<E> pred = succ.prev;
           // 创建新节点,pred为前驱，e为元素，succ为后继
           final Node<E> newNode = new Node<>(pred, e, succ);
        	// 插入链表节点的操作，改变后继节点的前驱节点属性，和前驱节点的后继节点属性
           succ.prev = newNode;
           if (pred == null)
               first = newNode;
           else
               pred.next = newNode;
        	// 记录元素数量 
           size++;
        	// 记录修改，和ArrayList里应该一致
           modCount++;
       }
   ```



- **E unlink(Node<E> x) **
  - `unlink`的方法都是用来删除链表中的元素
  - 类似的方法还有`unlinkFrist`和`unlinkLast`，但我不清楚为什么对象中已经记录了`first`和`last`两个节点，还需要在`unlinkFirst`的时候传入一个节点作为参数。

```java
E unlink(Node<E> x) {
    	// 获取x节点的所有属性
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;
		// 前驱为空，表示x即为first节点
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }
		// 后继为空 表示x即为last节点
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
		// 清空存储数据
        x.item = null;
   		// 修改元素个数和修改统计
        size--;
        modCount++;
        return element;
    }
```


### LinkedList相关问题
#### LinkedList vs ArrayList

![ArrayList vs LinkedList](https://chenbxxx.oss-cn-beijing.aliyuncs.com/ArrayList%26LinkedList.png)

​							<font size="2">(Array和ArrayList的结构是一致的.勉强能看)</font>

- 从结构上来说`LinkedList`是由双向链表实现,而`ArrayList`由动态数组实现,这是最根本的不同.

- 从元素存取来说;
  1. `ArrayList`继承`RandomAccess`接口支持随机访问,获取元素的时间复杂度为O(1),`LinkedList`不支持随机访问,获取元素只能逐个遍历,时间复杂度为O(n).
  2. `ArrayList`的插入操作还有数组边界的检查,扩容以及元素迁移等问题,而`LinkedList`仅需要改变前驱后继两个节点属性就足够.

