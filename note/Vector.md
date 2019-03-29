### Vector

- 2018-9-20
  - 刚看完`ArrayList`，感觉比以前看的时候清楚多了。
  - 先看看`transient`关键字。
- 2018-9-21
  - `vector`是在`jdk1.0`时引入，较之`ArrayList`还早。

---



##### Vector <font size=2>extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable </font>

- `Vector`是`ArrayList`之外的另一个`动态数组`集合。
- 若没有指定`capacityIncrement`增量，每次扩容就是*2，若指定增量则每次增加增量大小。
- `Vector`同样允许`null`值。
- 相比于`ArrayList`，最大的区别可能就是`Vector`是**线程安全**的，但也是因为为了线程安全，大量使用了方法级的`Synchronized`，所以执行效率较低。





#### 成员变量

- 成员变量中`capacityIncrement`为`ArrayList`中不存在的，在不为`0`时，则会成为每次扩容的增量。

```java
	// 底层结构和`ArrayList`一致，为数组
    protected Object[] elementData;
	// 元素个数
    protected int elementCount;
	// 容量的增量
    protected int capacityIncrement;
	// 最大容量
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```



#### 构造函数

- `Vector`提供了四种构造方式

  1. **Vector(int,int)**	指定`初始容量`和`增量`
  2. **Vector(int)**           指定`初始容量`
  3. **Vector()**
  4. **Vector(Collection<> extends E>)**        创建`入参集合`的对象实例

- **Vector(int，int)**

  - `Construct`都是大同小异的初始化`elementData`、`capacityIncrement`等底层属性，所以挑个默认构造函数   ٩(๑>◡<๑)۶ 

  ```java
    public Vector(int initialCapacity, int capacityIncrement) {
          super();
          if (initialCapacity < 0)
              throw new IllegalArgumentException("Illegal Capacity: "+
                                                 initialCapacity);
          this.elementData = new Object[initialCapacity];
          this.capacityIncrement = capacityIncrement;
      }
  ```

  - **剩余的构造函数都是`this`相互调用的就不贴代码了**
  - 默认的无参构造设置`initialCapacity`为**10**；



  #### 主要功能方法

  - 因为`Vector`和`ArrayList`实在太类似了。。所以有些方法就直接跳过了

    - `add`、`remove`、`index`等方法除了`Synchronized`修饰之外好像也差不多。。他娘的，`ArrayList`就是照抄的吧。
    - **`Vector`没有`SubList`相关代码，是完全继承于`AbstractList`的**。

  - **扩容方法**

    - **public synchronized void ensureCapacity(int minCapacity)**

    ```java
    // 除了`synchronized`外和`ArrayList`基本一致
    public synchronized void ensureCapacity(int minCapacity) {
            if (minCapacity > 0) {
                modCount++;
                ensureCapacityHelper(minCapacity);
            }
        }
    ```

    - **private void grow(int minCapacity)**

    ```java
    // 实际调用`grow(int)`的方法和`ArrayList`一致
    private void grow(int minCapacity) {
            // overflow-conscious code
            int oldCapacity = elementData.length;
        	// 最关键的扩容代码！！！
        	// 如果·增量· 不为0则每次都扩容指定大小，为0则*2扩充
        	// 如果增加过小，几次填充之后又要扩容，扩容中的复制原数组内容到新数组实在是很费时间
            int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                             capacityIncrement : oldCapacity);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    ```



  - **capacity()**

  ```java
  public synchronized int capacity() {
      /**  `ArrayList`中没有的方法
        * 返回的是底层数组的长度，而不是实际的元素个数。
        * `elementCount`表示的是实际的元素个数。
        **/
          return elementData.length;
      }
  ```



  - 别的方法下次再说。。发现不同在来回顾一下   ٩(๑❛ᴗ❛๑)۶


