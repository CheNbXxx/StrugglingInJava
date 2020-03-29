## Redis的底层编码相关整理

Redis优于其他K/V数据库的一个点就是它支持五种数据类型。

不同的类型底层都有2~3中编码格式，这是因为纯内存操作的原因，Redis需要一些短结构，在数据量并不大时尽量减少内存的使用。



---



### RedisObject

Redis中的每个对象都包装在`RedisObject`中。

```c
/** Redis 对象 */
typedef struct redisObject {
    // 类型 
    unsigned type:4;
    // 对齐位
    unsigned notused:2;
    // 编码方式
    unsigned encoding:4;
    // LRU 时间（相对于 server.lruclock）
    unsigned lru:22;
    // 引用计数
    int refcount;
    // 指向对象的值
    void *ptr;
} robj;
```



### 数据类型对应的编码格式

![digraph datatype {      rankdir=LR;      node[shape=plaintext, style = filled];      edge [style = bold];      // obj      redisObject [label="redisObject", fillcolor = "#A8E270"];      // type      node [fillcolor = "#95BBE3"];      REDIS_STRING [label="字符串\nREDIS_STRING"];     REDIS_LIST [label="列表\nREDIS_LIST"];     REDIS_SET [label="集合\nREDIS_SET"];     REDIS_ZSET [label="有序集合\nREDIS_ZSET"];     REDIS_HASH [label="哈希表\nREDIS_HASH"];      // encoding      node [fillcolor = "#FADCAD"];      REDIS_ENCODING_RAW [label="字符串\nREDIS_ENCODING_RAW"];     REDIS_ENCODING_INT [label="整数\nREDIS_ENCODING_INT"];     REDIS_ENCODING_HT [label="字典\nREDIS_ENCODING_HT"];     //REDIS_ENCODING_ZIPMAP [label="zipmap\nREDIS_ENCODING_ZIPMAP"];     REDIS_ENCODING_LINKEDLIST [label="双端链表\nREDIS_ENCODING_LINKEDLIST"];     REDIS_ENCODING_ZIPLIST [label="压缩列表\nREDIS_ENCODING_ZIPLIST"];     REDIS_ENCODING_INTSET [label="整数集合\nREDIS_ENCODING_INTSET"];     REDIS_ENCODING_SKIPLIST [label="跳跃表\nREDIS_ENCODING_SKIPLIST"];      // edge      redisObject -> REDIS_STRING;     redisObject -> REDIS_LIST;     redisObject -> REDIS_SET;     redisObject -> REDIS_ZSET;     redisObject -> REDIS_HASH;      REDIS_STRING -> REDIS_ENCODING_RAW;     REDIS_STRING -> REDIS_ENCODING_INT;      REDIS_LIST -> REDIS_ENCODING_LINKEDLIST;     REDIS_LIST -> REDIS_ENCODING_ZIPLIST;      REDIS_SET -> REDIS_ENCODING_HT;     REDIS_SET -> REDIS_ENCODING_INTSET;      REDIS_ZSET -> REDIS_ENCODING_SKIPLIST;     REDIS_ZSET -> REDIS_ENCODING_ZIPLIST;      REDIS_HASH -> REDIS_ENCODING_HT;     REDIS_HASH -> REDIS_ENCODING_ZIPLIST; }](https://redisbook.readthedocs.io/en/latest/_images/graphviz-243b3a1747269b8e966a9bdd9db2129d983f2b23.svg)



Hash

### Redis的数据类型的编码实现

列举各种数据类型对应的编码，以及转化规则，和编码间的区别。

所有的转化都是单项的，比如int转化为raw之后，

#### String

String相关的底层编码有`int`,`embstr`，`raw`。

embstr，raw的实现就是SDS（简单动态字符串），区别仅在于embstr会一次性分配`RedisObject`和`SDS`的空间。

使用规则：

1. long类型的整数，编码为int
2. 浮点数以及短字符串，编码为embstr
3. 大于44字节的长字符串，编码为raw

注意的是如果将int编码的数字append为普通字符串，int将会转化为raw。

原则上来讲embstr是只读的，如果对embstr的字符串进行append，它也会转化为raw，对int的append一样。

44字节这个阈值是在3.2版本以后的，之前为39。



#### List

List的底层编码有ziplist和linkedlist。

Hashlinkedlist就是普通的双端队列，ziplist也是一种短结构。

Redis中默认都是ziplist，触发结构变化的行为有以下几种：

1. 往List中添加长度大于`server.list_max_ziplist_value(默认64)`的值
2. kaList元素个数超过`server.list_max_ziplist_entries(默认512)`

简单来说，ziplist是一种类似数组的结构，分配的是一片连续的空间，访问的效率高，但是如果数据量大起来之后插入会变得非常耗时。

3.2版本之后统一使用了quicklist作为List的底层编码，quicklist可以简单理解为以ziplist为节点的链表，中和了linkedlist和ziplist的特点。



#### Hash

Hash的底层编码有ziplist和hashtable。

默认是ziplist，在以下几种情况下会转化为hashtable：

1. Hash中的某个Key或者Value大小超过 `server.hash_max_ziplist_value` （默认值为 `64` ）。
2. Hash的节点数量超过 `server.hash_max_ziplist_entries` （默认值为 `512` ）。



#### Set

Set的底层编码有hashtable和intset。

intset的底层实现也是一个数组，并且intset中只能保存long类型的元素。

根据第一个添加的元素，如果第一个元素为long类型，那么就是用intset，不然就是hashtable。

当下面几种情况发生时，intset会转化为hashtable：

1. intset中存储的元素个数超过`server.set_max_intset_entries`
2. set中添加了一个不是long类型的元素



#### ZSet















### 对应的数据结构

#### SDS  简单动态字符串

```c
typedef char *sds;
struct sdshdr {wei
    // buf 已占用长度
    int len;
    // buf 剩余可用长度
    int free;
    // 实际保存字符串数据的地方
    char buf[];
};
```

相对于C语的字符串类型来说，SDS具有以下优点：

1. SDS保存字符串的长度，所以Redis获取字符串长度的时间复杂度大部分都为O(1)。
2. 优化了APPEND操作，对字符串的APPEND会多分配所需一倍的空间。
3. 二进制安全。

#### quicklist  快速列表

#### skiplist  跳跃表

#### ht  字典

