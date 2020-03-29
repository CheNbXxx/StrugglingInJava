## 为什么LinkedList使用迭代器遍历更快

这里的更快是指和for循环的遍历相比。

2020-03-20，下午面试的时候问到了，就顺便整理一下，之前一直注意的是链表本身的实现。

写完发现好像也没什么好写的，源码量不多，结果显而易见。

<!--more-->

### 概述

LinkedList就是一个双向链表，也可以作为Queue和Deque使用。

和ArrayList相比，LinkedList的插入效率更高，只需要移动指针，也不需要扩容。

但从访问来说，ArrayList继承了RandomAccess，提供了很好的随机访问的复杂度，但是LinkedList并没有。



### 源码

以下是LinkedList的源码：

```java
// LinkedList.get
public E get(int index) {
      checkElementIndex(index);
      return node(index).item;
}

Node<E> node(int index) {
         // 该方法会根据index属于前半段还是后半段
    	//	 并借此选择向后还是向前遍历
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }
```

LinkedList的get方法作单次随机访问的时间复杂度为0(N)，M次的访问就变成M * O(N)。

而迭代器的代码如下：

```java
ListItr(int index) {
       // 可以看到初始化的时候如果指定某个下标的元素，
       //  迭代器也是通过一次又一次的遍历访问的。
       //   就是上面的node反法国 
      next = (index == size) ? null : node(index);
      nextIndex = index;
} 

public E next() {
       checkForComodification();
       if (!hasNext())// assert isPositionIndex(index);
            throw new NoSuchElementException();
      // 最后一次返回的元素 
       lastReturned = next;
     //  下一个元素  
       next = next.next;
       nextIndex++;
       return lastReturned.item;
 }
```

关键就在于LinkedList内部迭代器的构造了，它保存了下一个元素的指针，遍历的时候可以直接获取到下个元素，而不是重复的顺着链表遍历。



### 总结

ArrayList底层就是个数组，Java的数组是真数组，全部元素在一片连续的内存空间，根据下标就可以进行随机访问，迭代器反而复杂化了，所以ArrayListList的for循环遍历效率会更高。

LinkedList的底层是个双向链表，每次get访问都需要从头或尾重新遍历获取，而使用迭代器省略了获取下个元素时的遍历过程，保存了next指针可以直接获取，所以LinkedList使用迭代器的遍历速度会更快。

