# MySQL实战45讲

## 1. MySQL的基础架构

MySQL的总体结构包括Server层和存储引擎，Server层包括连接器，查询缓存(v8中完全删除)，分析器，优化器和执行器。

连接器管理连接和权限验证，分析器包括词法和语法分析，优化器预判执行效率选择查找方式(选择索引，所以走不走索引最终还是决定于优化器)，执行器连接存储引擎，返回结果。

查询缓存被删除的原因，主要还是效率低下，利用率也低，好像是通过一个语句的md5和结果做缓存，但是一个表有更新之后相关的缓存就全部失效了。



## 2. MySQL的日志系统

MySQL的系统可以简单分为Server层和存储引擎层，而`InnoDB`使我们最常用的存储引擎。

`binlog(二进制日志)`是Server层的，而redo log(重做日志)是`InnoDB`的，`InnoDB`还有另外一个`undo log(回滚日志)`。

`redo log`是`InnoDB`特有的日志，记录在一个固定大小空间中，以循环写的方式进行，所以多久时间都不会造成日志文件过大。

`check_point`到`write_pos`是未写入磁盘的日志，`write_pos`到`check_point`是已经写入磁盘的日志。

当`write_pos`追上`check_point`的时候就需要暂停操作，先等`check_point`推进刷盘。

![](https://static001.geekbang.org/resource/image/16/a7/16a7950217b3f0f4ed02db5db59562a7.png)

`binlog`是Server层的，所有的存储引擎公用，以追加写的形式，所以时间一久可能会导致日志过多。

`binlog`可以分为两类，一类index文件，是日志的索引，记录哪些日志文件正在使用。00000x文件则是具体日志文件，记录除查询之外的语句。

`redo log`是物理日志，记录的是SQL对某条数据的修改前后的值，`binlog`是逻辑日志记录，记录执行的SQL的具体逻辑。

