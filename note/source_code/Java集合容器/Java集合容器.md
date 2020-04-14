## Java集合容器

- 讲的是`java.util`包下的一些数据结构类.

![Java集合容器](/home/chen/github/_java/pic/Package%20util.png)



### 底层接口

上图就是整个集合容器的类图,其中最为顶层的接口有moshimoshi两个:

- Collection
- Map

Collection的子类中又有List,Set,Queue三个高级接口.



`List`表示列表结构,其实现主要有`ArrayList`和`LinkedList`.

`Set`表示集合结构,其中不含有重复的元素.

`Queue`表示队列结构,元素先入先出FIFO,又有双端队列(Deque)的实现.

Java中对栈的结构,使用`Stack`比较少,更多的是通过`Deque`实现.moshi

```java
 // 或者可使用ArrayDeque实现
        Deque<Integer> deque = new LinkedList<>();
        deque.push(1);
        deque.push(2);
        deque.push(3);
        while (!deque.isEmpty()){
            System.out.print(deque.pop() + ",");
        }
// 以上的输出为3,2,1
```





### RandomAccess

`RandomAccess`接口,就是个标识接口,表示提供快速随机访问.

它的子类实现有`ArrayList`,`Vector`,`RandomAccessSubList`.

其中`ArrayList`不用说,作为动态数组,其底层的实现就是一个数组,根据下标(偏移量)取值的时间复杂度为O(1).

可以在遍历时根据`list instanceof RandomAccess`来决定其遍历的方式,是使用循环遍历还是迭代器.







