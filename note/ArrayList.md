###       ArrayList源码

> 感觉相比起别的，我还是对数据结构的兴趣大一点~  (๑>︶<๑)
>
> 细读所以可能会有很多冗余的注释。

![Java集合框架类图](C:\Users\chenbxxx\Desktop\2243690-9cd9c896e0d512ed.gif)

---

#### ArrayList    <font size=2>extend AbstractList implement List,RandomAccess</font>

-  `ArrayList` 是最常用的一个集合，提供随机访问。
-  `ArrayList` 的底层结构是`数组`，初始化大小为**10**，每次扩容为原来的1.5倍。
   -  具体的算术代码为：`newCapacity = oldCapacity + (oldCapacity >> 1)`，不明白为什么是1.5+1
-  `ArrayList`是**非线程安全，且允许空值**。
-  `ArrayList` 在数据的`新增`，`删除`中 都会有`modCount++`，用此控制`迭代器`以及`子列表`与原数组之间的同步性，`checkForComodification()`方法会在`迭代器`和`子列表`的每个方法前面执行判断是否修改。

- 感觉上也可以说`ArrayList`就是一个会`自动扩容的动态数组`。


**关键属性**

```java
// 默认的底层数据大小
    private static final int DEFAULT_CAPACITY = 10;
// 从此可见，`ArrayList`的底层还是有数组来实现的
    transient Object[] elementData; 
// 数组大小
    private int size;
```



**构造方法**

- `ArrayList` 提供三种构造方法，逻辑不复杂，看看就好 ￣ω￣=

  - `public ArrayList(int initialCapacity)`

  ```java
  // 根据指定的数组大小初始化底层数组
  public ArrayList(int initialCapacity) {
          if (initialCapacity > 0) {
              this.elementData = new Object[initialCapacity];
          } else if (initialCapacity == 0) {
              this.elementData = EMPTY_ELEMENTDATA;
          } else {
              throw new IllegalArgumentException("Illegal Capacity: "+
                                                 initialCapacity);
          }
      }
  ```

  - `public ArrayList()`

  ```java
  // 使用默认的空数组初始化底层数组
      public ArrayList() {
          // 此处可能会奇怪 明明初始的是空数组，为什么会说初始容量为10m，看扩容相关的代码
          this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
      }
  ```

  - `public ArrayList(Collection<? extends E> c)`

  ```java
  // 使用已有的`Collection`为初始元素初始化底层数组
      public ArrayList(Collection<? extends E> c) {
          // 转化成数组`Array`
          elementData = c.toArray();
          if ((size = elementData.length) != 0) {
              if (elementData.getClass() != Object[].class)
                  // 利用工具类`Arrays`完成初始数据的拷贝
                  elementData = Arrays.copyOf(elementData, size, Object[].class);
          } else {
              // 初始化为空数组
              this.elementData = EMPTY_ELEMENTDATA;
          }
  }
  ```


**主要功能方法**

- 扩容相关方法
  - 扩容方法可以说是`ArrayList`中最重要的方法了。
  - 在每次插入元素的时候调用该方法检查容量是否足够，不足则扩充，才是`ArrayList`的实质。

```java
	// 确保底层数组容量足够
	// 参数：需要的最小容量
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

	// 计算容量，确保当底层数组对象为默认的初始空数组时，初始容量为`DEFAULT_CAPACITY`
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
	
    private void ensureExplicitCapacity(int minCapacity) {
        // 修改计数 是保证迭代器和子列表的基础
        modCount++;
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

	// 底层数组扩容的实际方法
	// 参数：扩容后的最小容量
   private void grow(int minCapacity) {
		// 记录旧容量
        int oldCapacity = elementData.length;
       // 新容量为旧容量的1.5倍
       // oldCapacity >> 1 相当于 oldCapacity * 2
        int newCapacity = oldCapacity + (oldCapacity >> 1);
       // 新容量小于增加的容量
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
       // 新容量超过最大容量 
       // MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // 复制旧数组,并扩容
       	// `copyOf`方法会把参数数组的元素复制到一个新的数组，新数组的大小为`newCapacity`
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

/**
 *  该方法不是扩容的..而是去空的
 *  因为每次不足时都会扩充为原来的1.5倍(也说是1.5+1)，即使此时只加了一个元素。
 *  所以肯定会有数组中肯定会有许多的空，放着浪费内存而且碍眼，必要时候可以去除。
 **/
  public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size);
        }
    }
```



- 添加/`addXXX`方法

  - 添加元素时需要先进行容量检查，不够则扩容。
  - 除了`grow()`方法中需要把`扩容`和`复制`合成一步使用的`Arrays.copyOf`，其他的数据移动操作全部使用`System.arrayCopy`完成。
  - `System.arrayCopy`的五个参数分别为：原数组，原数组复制起始位，目标数组，目标数组复制起始位，复制长度。
  - **public boolean add(E e)**

  ```java
    public boolean add(E e) {
        	// 确认底层数组的大小，不足则扩容
          ensureCapacityInternal(size + 1);  // Increments modCount!!
          elementData[size++] = e;
          return true;
      }
  ```

  - **public void add(int index, E element)**

  ```java
  public void add(int index, E element) {
       	// 检测是否超出数组大小，不足则抛异常
          rangeCheckForAdd(index);
  		// 确认底层数组大小，不足则扩容
          ensureCapacityInternal(size + 1);  // Increments modCount!!
        	// 将底层数组`elementData`的数组从`index`开始往后移一位
          System.arraycopy(elementData, index, elementData, index + 1,
                           size - index);
          elementData[index] = element;
          size++;
    } 
  ```

  - **public boolean addAll(Collection<? extends E> c)***

  ```java
    public boolean addAll(Collection<? extends E> c) {
          Object[] a = c.toArray();
          int numNew = a.length;
          ensureCapacityInternal(size + numNew);  // Increments modCount
        	// 将`a`数组全部拷贝到底层数组`elementData`尾部
          System.arraycopy(a, 0, elementData, size, numNew);
          size += numNew;
          return numNew != 0;
      }
  ```

  - **public boolean addAll(int index, Collection<? extends E> c)**

  ```java
   public boolean addAll(int index, Collection<? extends E> c) {
          rangeCheckForAdd(index);
          Object[] a = c.toArray();
          int numNew = a.length;
          ensureCapacityInternal(size + numNew);  // Increments modCount
          int numMoved = size - index;
          if (numMoved > 0)
              // 原数组后移`numNew`位
              System.arraycopy(elementData, index, elementData, index + numNew,
                               numMoved);
        	// 新增数据复制
          System.arraycopy(a, 0, elementData, index, numNew);
          size += numNew;
          return numNew != 0;
      }
  ```



- 删除`removeXXX`方法

  - **public E remove(int index)  删除index的元素，并返回其值  **

  ```java
  public E remove(int index) {
        	// 检查`index`是否在数组范围
          rangeCheck(index);
          modCount++;
       	// 获取旧值
          E oldValue = elementData(index);
  		// 将index以后的元素前移，即将index元素移置末尾
          int numMoved = size - index - 1;
          if (numMoved > 0)
              System.arraycopy(elementData,index+1,elementData,index,numMoved);
       	// 置空引用，size在此处减1
          elementData[--size] = null; // clear to let GC do its work
          return oldValue;
      }
  ```

  - **removeAll(Collection<> c) / retainAll(Collection<?> c) **

  ```java
  
  // 删除原数组中包含在集合c中的元素
   public boolean removeAll(Collection<?> c) {
       	// 判断是否为空 为空抛出NPE
          Objects.requireNonNull(c);
          return batchRemove(c, false);
      }
  
  // 删除原数组中不包含在集合c中的元素
    public boolean retainAll(Collection<?> c) {
          Objects.requireNonNull(c);
          return batchRemove(c, true);
      }
  
  // 批量删除操作
  // 参数: c: 比较是否存在的集合 complement：保留标准
  private boolean batchRemove(Collection<?> c, boolean complement) {
          final Object[] elementData = this.elementData;
          int r = 0, w = 0;
          boolean modified = false;
          try {
              for (; r < size; r++)
                  // 根据`complement`确定是存在还是不存在保留
                  // `removeAll`方法为`false`，表示留下不存在于集合c的元素
                  // `retainAll`方法为`true`，表示留下存在的元素
                  if (c.contains(elementData[r]) == complement)
                      elementData[w++] = elementData[r];
          } finally { 	// finally表示必须要有的步骤
              // 表示抛出异常等非正常中断操作则直接复制剩下的元素
              if (r != size) {
                  System.arraycopy(elementData, r,
                                   elementData, w,
                                   size - r);
                  w += size - r;
              }
              // 清空w以后的元素，即无用数据
              if (w != size) {
                  // clear to let GC do its work
                  for (int i = w; i < size; i++)
                      elementData[i] = null;
                  modCount += size - w;
                  size = w;
                  modified = true;
              }
          }
      	// w != size时返回true
      	// 即对原数组无操作时也返回false
          return modified;
      }
  
  	// 独立方法，我也不知道为什么`remove(index)`·要自己写...
     private void fastRemove(int index) {
          modCount++;
          int numMoved = size - index - 1;
          if (numMoved > 0)
              System.arraycopy(elementData, index+1, elementData, index,
                               numMoved);
          elementData[--size] = null; // clear to let GC do its work
      }
  
  ```



- 

- 迭代器 `Iterator`

  ```java
    private class Itr implements Iterator<E> {
        	// 下一个元素位置
          int cursor;       // index of next element to return
        	// 最后一个返回的位置索引
          int lastRet = -1; // index of last element returned; -1 if no such
        	// 更新次数标记，用来确定在使用迭代器遍历的时候有没有被修改
          int expectedModCount = modCount;
          Itr() {}
          public boolean hasNext() {
              return cursor != size;
          }
        	// 获取底层数组中的下一个元素，即`cursor`对应的元素
          @SuppressWarnings("unchecked")
          public E next() {
              //	这个方法比较简单，就是对比生成·Itr·实例的时候记录的`modCount`和当前是否一支
              checkForComodification();
              int i = cursor;
              if (i >= size)
                  throw new NoSuchElementException();
              // 获取当前ArrayList对象的底层数组对象
              Object[] elementData = ArrayList.this.elementData;
              if (i >= elementData.length)
                  throw new ConcurrentModificationException();
              cursor = i + 1;
              return (E) elementData[lastRet = i];
          }
  		// 删除
          public void remove() {
              if (lastRet < 0)
                  throw new IllegalStateException();
              checkForComodification();
              try {
                  // 删除的是原列表中元素
                  ArrayList.this.remove(lastRet);
                  cursor = lastRet;
                  lastRet = -1;
                  // 更新修改计数！！！
                  expectedModCount = modCount;
              } catch (IndexOutOfBoundsException ex) {
                  throw new ConcurrentModificationException();
              }
          }
      	// 重点方法用了final修饰
        	// 确认迭代器生成之后，原数组没有被修改过
          final void checkForComodification() {
              if (modCount != expectedModCount)
                  throw new ConcurrentModificationException();
          }
      }
  
  // Itr的升级版，和Itr一样都是操作原数组 也有检查更新的代码 具体不细讲
    private class ListItr extends Itr implements ListIterator<E> {
          // 可以指定开始遍历的位置
          ListItr(int index){
             super();
             cursor = index;
          }
          // 和原方法区别不大，就多了一层`modCount`的检查
          public boolean hasPrevious()
          public int nextIndex() 
          public int previousIndex()
          public E previous() 
          public void set(E e) 
          public void add(E e)
      }
  ```

- 子列表 `subList`

  - `subList` 通过在`ArrayList`的内部类`SubList`实现，`SubList`继承了`AbstractList`和`RandomAccess`
  - `subList` 和`iterator` 一样都是在原数组上操作，即所有的新增删除操作都会影响到原数组。
