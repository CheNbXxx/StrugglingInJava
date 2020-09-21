### Lab1



Lab1主要就是搭建一个MapReduce的计算模型。

这里先贴上测试通过的截图，Hahahahaha

 ![image-20200822155333913](/home/chen/github/_java/pic/image-20200822155333913.png)



Lab1需要实现的MapReduce是简易的，因为Map和Reduce的计算都在同一台机器上，并没有网络的影响因素。

按论文中所说，Map的入参是一堆的文件，然后输出中间文件，Reduce接受任务之后可能需要跨机器的寻找想要的文件，并通过网络传送。

没有网络因素就没有那些因为网络导致的不确定性，而且因为文件都在同台机器，所以即使一个Worker宕掉了，也可以继续使用它的文件。

MapReduce分为Worker和Master两种角色（实际中可能会有更多）,Worker用来执行Map或者Reduce方法，而Master则是任务状态的调度和汇总。

另外Lab1还要求的简单的容错，就是它会简单模拟宕机和网络延迟的情况，Lab文档中也给出的希望的超时时间，10s，即超过10s未报告完成任务，就当任务失败。





### Master和Worker的基本数据结构

首先是Master，Master作为一个中心的调度程序，那么它就必须持有Map和Reduce的任务列表。

每个任务都包括需要处理的文件，文件状态，任务Id，上次调度成功的时间等。

除了任务列表之外，为了方便期间，Master还需要持有一个递增变量，已在Worker注册的时候分配一个唯一变量。

而Worker只需要保存Map和Reduce的函数就好了，还有WorkerId。



### 简单的流程

1. 创建Master，指定文件列表和nReduce(reduce任务的个数)，根据文件列表生成Map任务列表，初始化其他属性。
2. 创建Worker，保存Map和Reduce方法，然后就是向Master**注册**获取一个唯一的WorkerId，随后开始循环**获取任务**。
3. Master的任务下发需要做的就是遍历Map任务列表，找到一个状态为待分配的或者上次调度时间超过10s，且状态为执行中的，然后返回给Worker。
4. Worker处理下发的文件，读取出所有内容然后调用Map方法，最后将K/V对根据提供的`ihash(key) % nReduce`方法划分为不同的文件。
5. Worker完成任务之后**上报**，Master记录任务产生的文件名，然后添加到不同的Reduce任务列表，之后Worker会继续申请任务。
6. Map任务全部执行完之后开始执行Reduce的任务，流程差不多，遍历读取中间文件中所有的KV，排序然后调用Reduce方法，最后输出文件并上报。
7. 循环往复直到任务全部FINISH。



采用的是更为简单的流程，即Map全部执行完毕才继续执行Reduce。



### 遇到的坑

1. 文件名的确定

Map任务生成的中间文件，命名为`mr-{MapTaskId}-{ReduceTaskId}`。

最开始使用的`mr-{workerId}-{reduceTaskId}`导致的在同个Worker处理多个任务时文件被覆盖而数据错误。

2. Go的语法问题

因为主要还是用Java作为开发语言，一下子切到Go这个语法的熟悉度有点够呛。



