# MySQL-InnoDB日志相关总结



- 最近入职的公司有评职称环节,然后期间被问到相关问题,答案就在嘴边却说不出来,还是不够熟悉吧.
- 下文基于InnoDB中的相关日志展开,工作期间基本都是使用默认的InnoDB,对其他的了解不多<font size=1>(惭愧)</font>.
- 对InnoDB存储引擎的理解基本来源于`MySQL-InnoDB存储引擎`,极客时间的`MySQL实战45讲`,以及一些平时浏览的博客.



---

<!-- more -->

[TOC]



## 概述

InnoDB的中主要涉及到日志有三类,分别是

- [binlog - 二进制归档日志](#binlog - 二进制归档日志)
- [redo log - 重写日志](#redo log - 重写日志)
- [undo log - 重做日志](#undo log - 重做日志)





## binlog - 二进制归档日志

binlog是MySQL中的归档日志,在Server层实现,所有在MySQL中执行的DML语句(除查询之外)都会在此留下痕迹.

MySQL的主从复制中,从库就是通过获取主库的binlog,来完成数据的同步.

binlog属于逻辑日志,单独的binlog并不能提供持久性的保证.

另外MySQL的binlog有两种记录格式,Statement和Row.



## redo log - 重写日志

redo log是InnoDb特有的日志,是事务持久性的实现关键.

redo log是物理日志,其中记录的是语句对于数据页面的修改.

InnoDB遵循`WAL(Write-Ahead Log)`原则,简单来说就是先写日志,在写数据,事务提交的时候日志文件必须落盘.

当一条Update语句执行时,InnoDB会先在redo log记录,并且修改内存缓冲区中对应的数据页,就算执行完毕了.

此时就算机器掉电,重启之后也可以根据redo log恢复已提交的事务状态.

InnoDB中的redo log在占据了一块固定大小的区域,并且循环读写,可以简单理解为下面的环形结构,实际实现中一般分为2-4个文件.<font size=2>(下图是MySQL实战45讲中的,看着不错借来用用)</font>>

![img](../../pic/16a7950217b3f0f4ed02db5db59562a7.png)

其中write pos为当前写入的地址,check point表示当前正要清理的地址,write pos和check point之间就是可写范围.

如果write pos追上check point,就需要先暂停手上的事务,先将check point向前推进.

推进check point的就是刷新脏页(刷盘)的过程,因为此时要把记录清楚掉,就一定要保证对应的数据页已经写入了磁盘,不然若是调电,脏页上的数据就会丢失.

另外redo log还有另外一个作用**就是将随机写变成了顺序写.**

如果没有redo log,那么要实现数据的持久化,就必须要在事务提交的时候将对应的脏页落盘,单个事务可能涉及到很多页面,这些IO就是随机写IO.众所周知,磁盘的随机写效率有多低.

而此时,每次更新都只需要在read log buffer中修改,并且同步到磁盘就好了.

说到这里,还有一个重要的redo log的参数:innodb_flush_log_at_trx_commit,根据该参数设置的不同,InnoDB提供的持久化的程度也不同.

该变量有0,1,2三个值分别代表不同的落盘策略.

了解参数之前需要先理解日志落盘的过程,redo log buffer保存在InnoDB的内存缓冲池中,第一步`write`操作之后,该部分内容会从InnoDB的缓冲池被写入操作系统的文件缓冲池,只有在第二步`fsync`之后,才是真正的落盘,持久化到磁盘上.

innodb_flush_log_at_trx_commit=0,表示第一步和第二步都是定时完成的,事务提交的时候不会有别的操作,后台线程会周期性的将redo log buffer持久化到磁盘上,周期大约为1s.

innodb_flush_log_at_trx_commit=1,表示每次的事务提交都会执行`write`和`fsync`.

innodb_flush_log_at_trx_commit=2,表示每次事务提交的时候都会执行`write`,将数据写入操作系统的文件缓存池中,之后后台线程会每隔一秒调用`fsync`.

设置为1时能保证即使在掉电的情况下,都不会造成数据的丢失,但是性能上来说会有打蛮大的折扣.



## 二阶段提交

因为一次MDL涉及到多个类型的日志,所以如何保证日志内容的一致性也就非常关键了.

二阶段提交可以说是两个模块之间协商的经典模式了.

这里不得不再次使用MySQL实战45讲的图片.

![img](../../pic/2e5bff4910ec189fe1ee6e2ecc7b4bbe.png)

由上图可知,更新语句执行的时候先写redo log,再写binlog,最后在提交redo log,commit事务.

binlog在redo log之后写的原因是因为binlog另外负责了主从的数据同步问题,如果先写如binlog,那么就会被从库获取到,而进一步执行,如果此时系统在redo log写入之前崩溃,那么更新的数据就丢失了,之前也说过binlog无法提供崩溃恢复的功能,但是从库却已经更新了,这就造成了主从数据的不一致,还有redo log和binlog的日志不一致.





## undo log - 重做日志

引入undo log实际上是为了实现事务的原子性,以及MVCC(多版本并发控制).

为了保证原子性,就必须保证事务的出错的时候可以回滚,为了实现可回滚的概念,InnoDB会在事务修改前将旧事务记录下来,就形成了undo log.

对于MVCC来说,undo log在插入时会携带一个事务id的字段,该字段由InnoDB下发,并且单调递增,InnoDB内部也会维护一个活动中的事务id数组.

在查询的时候会根据不同的隔离级别和当前事务Id判断数据的可见性,也就是创建了一个类似于数据视图的概念.

如果当前的数据行对当前的事务并不可见,InnoDB则会按照undo log的列表熟悉怒逐个恢复到可见为止.





## 参考博客

- [详细分析MySQL事务日志(redo log和undo log)](https://www.cnblogs.com/f-ck-need-u/p/9010872.html)
- [MySQL · 引擎特性 · InnoDB 崩溃恢复过程](http://mysql.taobao.org/monthly/2015/06/01/)
- [MySQL · 引擎特性 · InnoDB undo log 漫游](http://mysql.taobao.org/monthly/2015/04/01/)
- [MySQL · 引擎特性 · InnoDB redo log漫游](http://mysql.taobao.org/monthly/2015/05/01/)