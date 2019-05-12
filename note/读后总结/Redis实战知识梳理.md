其实…这本书我还没有看完,只看了2/3的样子。

但是最近工作实在忙的四脚朝天没太多时间看,而且内容有些可能感觉不是很用的上。

所以前面的基础或者常用部分就先做个简单总结。

另外一本设计与实现我也不知道该放到第几本看了。



### Redis基础

#### Redis简述

`Redis`是**内存型的非关系型(non-relational database),NoSQL,K/V数据库**.

区别于`MySQL`之类的关系型数据库,`Redis`中没有明确的表的概念。

`Redis`中常用的数据结构有五种:String,Hash,List,ZSet,Set



#### Redis中的数据结构以及相关命令

##### String 

说是字符串,但是String类型的数据结构可以保存**字符串,整型(int64),浮点型**的数据。

`Redis`中的键都是String类型的。

| 命令            | 行为                                           | 示例                              |
| --------------- | ---------------------------------------------- | --------------------------------- |
| GET / SET / DEL | 获取、设置、删除键中的String值                 | GET keyName                       |
| INCR / DECR     | 将键中保存的值+1、-1                           | INCR keyName                      |
| INCRBY / DECRBY | 将键中保存的值增加、减少n                      | INCRBY keyName n                  |
| INCRBYFLOAT     | 将键中保存的值增加一个浮点数n                  | INCRBYFLOAT keyName n             |
| APPEND          | 向键中保存的值追加部分String                   | APPEND keyName value              |
| GETRANGE        | 获取键中值的部分,可理解为SubString,左闭右闭    | GETRANGE keyName start end        |
| SETRANGE        | 设置键中值的部分,偏移量offset到末尾替换为value | SETRANGE keyName offset value     |
| SETBIT          | 以键中值作为二进制字符串，并设置偏移量的值     | SETBIT keyName offset value       |
| GETBIT          | 获取二进制字符串中值为1的个数，起始,末尾可选   | GETBIT keyName [start,end]        |
| BITOP           | 一个或多个keyName进行逻辑操作,结果存入dest     | BITOP operation destName key…Name |

###### 注意

1. INCR*都为原子操作。
2. INCR*等命令，如果键不存在则会以0为默认初始值，键中的值无法翻译为数值则报错。
3. SETBIT / GETBIT 算是位图操作，作为一种类似签到的记录可以节省大量的资源，而且操作简单。
4. `BITOP`的operation包括**AND,OR,XOR(异或),NOT**。