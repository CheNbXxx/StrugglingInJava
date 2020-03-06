### 和Key相关的问题整理



### 大Key问题

首先大Key问题并不是指Key的长度，而是以下的问题：

1. 单个Key对应的Value很大
2. Set，List，ZSet，Hash等集合类型中元素过多

Redis是单线程运行的，如果一次处理过大的value就会导致服务停顿。

因此在Redis中要尽量避免大Key，对于已经存在的大Key



