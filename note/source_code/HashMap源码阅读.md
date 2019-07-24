# HashMap源码

- 刚看一眼。。感觉以我的水平不能完全理解，只能欣赏一边了解大概的意思了
- 一些相关的问题会记录在结尾。

---

##### HashMap <font size=2>extends AbstractMap<K,V> implements Map<k,V>,Cloneable,Serializable</font>



###### 静态常量

```java
	// 默认的容量，底层数组的默认大小，1 << 4 = 16
	static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; 
	// 最大容量
    static final int MAXIMUM_CAPACITY = 1 << 30;
	// 默认负载因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
	// `bucket`的元素大于该数，转化为红黑树结构，还有数组容量的要求。
    static final int TREEIFY_THRESHOLD = 8;
	// `bucket`的元素小于改数，转化为链表节点
    static final int UNTREEIFY_THRESHOLD = 6;
	// `bucket`结构从链表->红黑树的最后数组容量
    static final int MIN_TREEIFY_CAPACITY = 64;
```

###### 成员变量

```java
 	// 	可见`HashMap`的底层还是依赖于数组完成的
    transient Node<K,V>[] table;
	// 键值对的`Set`
    transient Set<Map.Entry<K,V>> entrySet;
	// 元素数量，集合大小
    transient int size;
	// 修改计数，`fast-fail`机制关键属性
    transient int modCount;
	// 扩容阈(yu4)值，对键值对的数目大于该值会进行扩容
    int threshold;
	// 负载因子
    final float loadFactor;
```

###### 构造函数

- **public HashMap()**	无参构造

```java
    public HashMap() {
        // 仅仅指定了加载因子
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
```

- **public HashMap(int initialCapacity, float loadFactor)**	

```java
   public HashMap(int initialCapacity, float loadFactor) {
        // 三个判断确保初始容量的范围正确
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        // 指定加载因子
        this.loadFactor = loadFactor;
        // 指定扩容阈值
        this.threshold = tableSizeFor(initialCapacity);
    }
```

- **public HashMap(int initialCapacity) **

```java
   public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
```

- **public HashMap(Map<? extends K, ? extends V> m)**   以一个`Map`为参数	 

```java
public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
```

- **在四个构造函数中都没有初始化底层的`table`，因为在第一次调用`putVal（）`时才进行的初始**

###### 节点内部类

- **Node**
  - 是`HashMap`最底层的元素类，键值对，实现了`Map`的`Entry`接口。
  - 是数组中的`bocket`的**链表**元素，`RB-Tree`有另外的节点类。

```java
   static class Node<K,V> implements Map.Entry<K,V> {
       	// 注意：此处不是该`Node`的哈希值，而是`key`的哈希值
        final int hash；
        final K key;
        V value;
       	// 下个节点
        Node<K,V> next;
        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }
       // 节点的`hash`是`key`和`value`的哈希值的异或
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }
        // 重新设置`Value`并返回旧值。
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }
        public final boolean equals(Object o) {
            // 引用地址一致
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
```

- **TreeNode**

  - `RB-Tree`的基本元素结构。
  - 该内部类很详细，包括了树结构的`CRUD`一套和关键方法。
  - 继承于`LinkedHashMap`的内部类`Entry`，但`Entry`又是继承于`HashMap`的`Node`。(；′⌒`) 好他娘的乱
  - **成员变量和构造函数**

  ```java
      static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
          TreeNode<K,V> parent;  // red-black tree links
          TreeNode<K,V> left;
          TreeNode<K,V> right;
          TreeNode<K,V> prev;    // needed to unlink next upon deletion
          boolean red;
          
          TreeNode(int hash, K key, V val, Node<K,V> next) {
              super(hash, key, val, next);
          }
  ```

  - **final TreeNode<K,V> root() **

  ```java
  		// 遍历返回树的跟节点，判断依据是没有父节点
          final TreeNode<K,V> root() {
              for (TreeNode<K,V> r = this, p;;) {
                  if ((p = r.parent) == null)
                      return r;
                  r = p;
              }
          }
  ```

  - **

  ```java
    final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
              TreeNode<K,V> p = this;
              do {
                  int ph, dir; K pk;
                  TreeNode<K,V> pl = p.left, pr = p.right, q;
                  if ((ph = p.hash) > h)
                      p = pl;
                  else if (ph < h)
                      p = pr;
                  else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                      return p;
                  else if (pl == null)
                      p = pr;
                  else if (pr == null)
                      p = pl;
                  else if ((kc != null ||
                            (kc = comparableClassFor(k)) != null) &&
                           (dir = compareComparables(kc, k, pk)) != 0)
                      p = (dir < 0) ? pl : pr;
                  else if ((q = pr.find(h, k, kc)) != null)
                      return q;
                  else
                      p = pl;
              } while (p != null);
              return null;
          }
  
  ```

- 树操作很麻烦的样子。。等以后再看 ╮(╯﹏╰）╭



###### 内部方法类

- **static final int hash(Object key)**	`扰动函数`<font size=1>（来源知乎）</font>
  - 代码从功能上讲就是将`hashCode`方法返回的`int`的高16位和低16位进行异或。
  - 我们都知道`HashMap`是采用哈希值来确定`Key`在数组中的位置，所以如果想要`HashMap`的效率足够的高就必须要减少碰撞的发生，使元素均匀的分散在数组中。
  - `putVal()`方法中也使用`tab[i = (n - 1) & hash]`来完成对数组下标的获取，`n`为数组大小，同时数组大小通常要求定义为`2的整次幂`，减去`1`之后就变成了`n个0,32-n个1`的低位掩码，同时在对`hash`做`&`操作，也可以理解为取`hash`的低`32-n`位。返回的`hash`为32位的`int`数，而数组长度初始只有`16`，，例如数组长度为16时，仅使用`hash`的低4位，剩下的28位都没有产生任何的作用。因此采用这种**高16位和低16位混合运算**的方法，充分利用整体`hash值`，有效的减少冲突。

```java
 static final int hash(Object key) {
        int h;
     	// hashCode返回的int(32位)的高16位和低16位异或
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

​	

- **static final int tableSizeFor(int cap)**
  - 一段我认为非常牛逼的代码，作用为**获取一个比`cap`大的最小的2的整数次幂**。
  - 原理我也讲不出来。。

```java
  static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```



- **final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict)**
  - 向`HashMap`中插入数据的具体实现方法，流程如下：
    1. 检查是否创建`table`数组，未创建则调用`resize()`初始化
    2. 根据`hash`得到`table`数组中的下标，并获取`bocket`,为空则直接赋值
    3. 不为时还会判断是否为`TreeNode`，树节点有另外的处理方式。
    4. 遍历链表，判断链表是否大于`TREEIFY_THRESHOLD`，是则将结构转化为红黑树,同时判断是否存在`key`值相同的键值对，如果发现遍历到末尾则会直接添加节点。
    5. 此时已遍历时的下个节点`e`为判断依据
    6. 若存在则表示存在`key`值相同的键值对,根据`onlyIfAbsent`参数，决定是否覆盖.
    7. 若不存在表示添加了元素，则进行一些后续处理。
  - 注意 ：**覆盖旧值不会引发`modCount`自增，所以在迭代器遍历原值改变的时候不会报错。**

```java
  final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
      	// hash`  表示`key`的哈希值
	    // `key,value`   一个键值对
        // `onlyIfAbsent `  是否保留旧值，为`false`时不保留，当旧值为`null`时肯定覆盖。
	    // `evict`   在最下面的` afterNodeInsertion(evict);`时调用，暂时不知道干吗用的
        Node<K,V>[] tab; 
        Node<K,V> p; 
        int n, i;
      	// 1.给`tab`和`n`赋值
      	// 2.在底层的`table`未创建时会调用`resize()`进行初始化
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
      	// 采用的 `(n - 1) & hash`的方式确定`key`在底层`table`的下标
      	// 这里用的不是我想像中取余的方式，但因为要求`n`是2的整数次幂，所以会发现
      	// `(n - 1) & hash` === `hash % n`，但是逻辑运算更契合底层结构速度更快。
      	// 获得下标对应的`bocket`，也就是数组元素，若为空表示还没有数据，则直接新建`Node`
      	// 并添加到该下标位置。
      	// 嗯。。顺带对`p`进行了赋值
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            // 长逻辑表达式前面判断`hash`，后面判断`key`
            // 对`key`值是否相等的判断是引用和`equals()`方法一起的。
            // 为`true`表示原数组中存在对应的`Node`，赋值给`e`
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            // 判断是否是树节点，如果是则调用树节点的插入方法
            else if (p instanceof TreeNode)
                // 返回的e应该是插入的节点，现在未知
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            // 注意：因为在这个if已经判断过`TreeNode`，所以之后的都是`链表`的操作。
            else {	
                // 无限循环，只等循环体`break`
                // `binCount`同时也记录元素个数
                for (int binCount = 0; ; ++binCount) {
                    // `p`在首次时是`bocket`中链表的头元素
                    // 若`p.next`为空，表示已到链表的尾节点
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 当`binCount`也就是元素个数大于`TREEIFY_THRESHOLD`时，
                        // 将结构转化为`RB-Tree`，减1是因为`binCount`基底为0，
                        // 此处仍有以为，转化为树结构不应该还要数组大于64吗？
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            // 链表转化为`RB-Tree`的方法，代查看
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 若链表中发现有相同的`key`直接跳出，此时`e`就是包含`key`一致的节点
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    // 迭代循环
                    p = e;
                }
            }
            // 后面也写着了。。`e`不为空表示`key`已存在
            if (e != null) { // existing mapping for key
                // 获取旧的value
                V oldValue = e.value;
                // 此处可以知道参数`onlyIfAbsent`的作用：是否覆盖旧值，为`true`时不覆盖。
                // 注意： `HashMap`允许null值，当旧值为null时肯定覆盖
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                // 修改Node之后的操作吧，待查看
                afterNodeAccess(e);
                return oldValue;
            }
        }
      	// `fast-fail`的关键变量自增，可以发现在覆盖旧值的时候不会引起`modCount`的变化。
        ++modCount;
      	// `map`大小先自增在比较，大于扩容阈值则进行扩容。
        if (++size > threshold)
            // 扩容的具体方法
            resize();
      	// 插入之后的方法，代查看
        afterNodeInsertion(evict);
        return null;
    }
```



- **final Node<K,V>[] resize()**
  - **扩容**的具体实现函数，只有在`size`大于`threshold`的时候会被调用。
  - 该方法也承担了**首次**调用`putVal`时，**初始化底层数组**的责任。

```java
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
    	// 记录旧容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
    	// 记录旧阈值
        int oldThr = threshold;
        int newCap, newThr = 0;
    	// 旧容量大于0，表示底层的`table`已经初始化
        if (oldCap > 0) {
            // 旧容量已经等于最大容量，改变阈值为int的最大值，并直接返回旧`table`
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // `newCap = oldCap << 1` 表明新容量为旧容量的一倍
            // 判断新容量是否小于最大容量，并且旧容量是否大于初始容量，
            // `true`则将就阈值也加倍。
            // 若为`false`，`newCap`也已经加倍。
 @1         else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
    	// 执行该段表示旧容量为0，走的是含参构造
 @2     else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
    	// 执行下面的时候表示`oldThr`和`oldCap`都不大于0，即底层数组为初始化，阈值也没有制定
    	// 走的是无参构造
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            // 默认阈值为 0.75 * 16 = 12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
    	// 新阈值为0的情况，仅仅出现在`@1`出判断`false`，或者`@2`处执行。
    	// 1. 新容量大于最大值
    	// 2. 旧容量小于默认容量（16）
    	// 3. 通过含参构造，即底层数组为初始化，但阈值已指定的时候
        if (newThr == 0) {
            // 指定新阈值，为新容量*加载因子。
            // 当新容量大于最大容量时，直接以int的最大值当做新阈值。
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
    	// 创建新数组
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
    	// 用脚想也知道。。是把原数组的信息搬到新数组
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    // `bocket`只有一个链表的头节点
                    if (e.next == null)	
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    // 不只有头节点的链表
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            // 因为数组下标是用[hash & (n - 1)]确定的，
                            // 所以当`n`改变时，下标的也会随之改变。
                            // eg. `hash`=33，`n`从32->64时下标就会1->33
                        	next = e.next; 
                            // `e.hash & oldCap` 是为了确认新数组下标是否属于旧数组的
                            // 若为0，则表示元素在原数组是放得下的有位置的，
                            // 若不为0，表示元素是在原数组范围之外的。
                            // 该`do..while`循环作用即为：按照`e`的hash是否在原数组，拼装链表
                            // 其实同一个桶，下标肯定是一致的，不知道为什么要重新计算。
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 在原数组放得下的元素直接原下标放到新数组
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 原数组就放不下的元素，放在新数组，下标即为`j + oldCap`
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```

- **final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) **
  - 批量插入数据的方法，仅在以`Map为参数的构造函数`和`putAll`里有调用。

```java
 final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            // 底层数组未初始化
            if (table == null) { // pre-size
                // 大小为`参数map`除以加载因子，加1
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                // 加载因子是不管哪个构造函数都会初始化的，肯定不回为空就对了
                if (t > threshold)
                    // 此处没有创建底层的`table`，还是指定的扩容阈值
                    // 创建的操作还是存在于`putVal`
                    threshold = tableSizeFor(t);
            }
            // 需要空间超过阈值，所以需要扩容
            else if (s > threshold)
                resize();
            // 遍历和`putVal`的操作
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }
```

- **final Node<K,V> getNode(int hash, Object key)**
  - 获取某个节点的底层操作方法。
  - 判断`key`是否一致的步骤
    1.  判断`hash`是否一致。
    2. ·判断`key`的引用 -> 判断`key`的实际值，满足其中之一就是一致。

```java
 final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; 
     	Node<K,V> first, e; 
    	int n; 
     	K k;
     	// 1. 底层`table`不为空，且长度大于0。
     	// 2. 参数`hash`计算得下标的元素在原数组中存在。
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            // 判断`首节点`的`hash`和`key`值， 注意此时`k`已经被赋值
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
               	// 区分是否是树节点，树节点有另外一套遍历查找方法
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // `do...while` 遍历查找链表
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```

- **final Node<K,V> removeNode(int hash, Object key, Object value,boolean matchValue, boolean movable)**
  - 删除节点的底层操作方法。
  - 入参
    - `key`    需要查找的键值
    - `hash`     `key`的hash值
    - `value`   删除的值，仅在`matchValue`为`true`的时候生效
    - `matchValue`    若为`true`表示当`value`也相等时才删除
    - `movable`

```java
final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab;
    	Node<K,V> p;
    	int n, index;
    	// 确认底层的数组已经初始化，且`hash`对应的下标有值
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; 
            K k;
            V v;
            // 1. 判断头节点是否符合要求，
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            // 2. 判断后继节点是否存在
            else if ((e = p.next) != null) {
                // 调用节点自身的方法获取`node`，此时`p`为树的根节点
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    // `do...while`遍历链表，找到`key`对应的节点
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // 判断是否获取到节点
            if (node != null &&
                // 获取到节点之后，如果`matchValue`为`true`，还要继续匹配`value`
                (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    // 调用树节点自身的方法删除，movable表示是否要移动其他树节点
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }
```





###### `JDK1.8`新增功能方法

- **public V getOrDefault(Object key, V defaultValue)***
  - 获取`key`对应的值，如果不存在则返回`defaultValue`
  - 看到`@Override`就知道是从父类继承的。。是从`Map`街扩继承的，并不知道有什么太大的作用。

```java
 @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }
```

- **public V putIfAbsent(K key, V value)**

  - 填充`null`的方法，查找`key`对应的节点，如果`value`为`null`，则用参数`value`填充

  - 因为在`putVal`中关于值得覆盖判断为`if (!onlyIfAbsent || oldValue == null)`，

    	所以此处调用时是`onlyIfAbsent`为`true`，仅用后表达式的作为判断依据。

```java

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }
```

- **public boolean remove(Object key, Object value)**
  - 删除节点的方法，不难就不解释了。
  - 两个`true`参数，第一个代表地是否需要匹配`value`值，第二个表示删除后是否需要移动其他节点。

```java
@Override 
public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }
```

- **replace系列方法**

```java
 	// 替换提前还要对比`oldValue`是否一致
	@Override	
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }
	// 直接替换，不管旧的`value`
    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }
```









#### HashMap相关问题

##### HashMap的容量为什么一定要是2的次幂

- 容量为2次幂有两个优点
  1. 在下标运算的时候使用`(length - 1) & hash)`代替`hash % length`，相对来说位运算性能更佳，速度更快。
  2. 而在采用`(length - 1) & hash`的方式计算下标之后，如果不是二次幂的容量，出现碰撞的几率将会大大增加，例如我们取17作为容量(`(17 -1) => 0001000`)，经过`&`与运算，可以想象会有一大批的元素直接挂在0号桶。
- 可以说这是一整套的策略，如果使用`hash & length`的话，也不用要求容量一定是二次幂，但各方面的性能总是会差一点的。







- 还有一些新方法以后再整理，还有就是`TreeNode`里的一些关于树的操作方法，太久没碰数据结构了不好理解，也放在以后再看吧。
