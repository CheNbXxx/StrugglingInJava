### Stck源码

- 2018-9-21 

  - `Stack`继承于`Vector`，仅实现了其中五六个方法，所以应该简单地。。。

---



#### Stack <font size=2>extends Vector</font>

- `Stack`继承于`Vector`，说明`stack`也具有`Vector`的包括`add`、`remove`等在内的所有方法，也就是说`Stack`也能被当做是一个动态数组使用，虽然`Vector`也已经不常用了。
- 除此之外`Stack`，扩展了几个方法，使其成为一个**LIFO的栈**

- 构造函数只有默认无参构造，且没有自定义的成员变量，可以说很简单了。





##### 主要方法

- **public E push(E item)** 	入栈方法

```java
   // 入栈，放入栈顶
	public E push(E item) {
        
        addElement(item);
        return item;
    }
	// 该方法和`add(*)`方法仅差一个返回值
	// 可以看出Stack也是线程安全的，存取效率都不如`ArrayList`
	// 所谓的栈底层也是数组，每次添加都是添加到数组最后，可想在获取时应该也是一样
    public synchronized void addElement(E obj) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
    }
```

- **public synchronized E peek()**	 	查看栈顶元素

```java
// 线程安全的方法  
// 获取数组最后一个元素
public synchronized E peek() {
        int     len = size();
        if (len == 0)
            throw new EmptyStackException();
        return elementAt(len - 1);
    }
// 该方法和`get(int)`除了抛出异常不一样，其他一致
 public synchronized E elementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }

        return elementData(index);
    }
```


- **public synchronized E pop()**		弹出栈顶

```java
public synchronized E pop() {
        E       obj;
        int     len = size();
    	// 获取最后一个元素
        obj = peek();
    	// 删除最后一个元素
        removeElementAt(len - 1);
        return obj;
    }
// 除了没有返回值，其他和`remove(int)`一致
  public synchronized void removeElementAt(int index) {
        modCount++;
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " +
                                                     elementCount);
        }
        else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, j);
        }
        elementCount--;
        elementData[elementCount] = null; /* to let gc do its work */
    }
```



- 看到这，我突然感觉很奇怪，`Vector`里为什么有两套新增和删除的操作方法，类似`addElement`和`add`，太过于冗余了，在`Stack`里也是如此，`pop`的弹出操作，明明可以直接使用`remove`方法，删除并获取返回值，没必要使用`element`这套方法先获取了在删除，感觉这两个类可能要被淘汰了吧。。
- `AbstractList`和`ArrayList`都为`jdk1.2`的产物，而`Vector`和`Stack`都是`jdk1.0`的，想想可能是为了兼容新版本吧，以后看源码看来也要看下`version`信息了。
- 发现集合框架里面其他部分基本都是`1.2`的。。。