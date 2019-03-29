#### AMQP

- 最近在学`RabbitMQ`,所以感觉也要稍微学习一下`AMQP`,这个他依据的协议.
- 首先,`AMQP`的全称是**Advanced Message Queuing Protocol(高级消息队列协议)**,是处于第七层(应用层)的协议.



#### AMQP整体结构

![AMQP](https://github.com/CheNbXxx/StrugglingInJava/blob/master/img/AMQP%E5%8D%8F%E8%AE%AE%E8%AF%A6%E8%A7%A3.png)

- 由上图我们可以看到`AMQP`的一些组成部分:

- Producer/Consumer

  ​        两者一起构成了`AMQP`的客户端,分别是消息的生产者和消费者,通过`Connection`和`Borker`相连.通过在`Connection`之上建立的`Channel`和`Borker`进行交互.

- Message

  ​	是`AMQP`中交互的主体,简单来看可以**分为`Header`和`Body`两部分**组成,其中**`Header`中包含了`Routing Key`**也就是路由规则.

- Exchange

  ​	**交换机**,`Exchange`在`AMQP`协议中像是一个邮差的工作,一个`Exchange`可以同时绑定多个`Queue`,也可以绑定到其他的`Exchange`.当建立绑定后`Exchange`会根据对方的Binding Key`建立自	己的路由表.

  ​	**当接收到`Message`时,会根据一定的路由规则发送到特定的`Exchange`或者`Queue`中.**

- Queue(Message Queue)

  ​	**消息队列**,在`AMQP`协议中相当于一个邮箱的角色,负责暂时存储`Message`.和`Exchange`之间通过绑定实现关联.

- Broker

  ​	`Broker`是`AMQP`的服务端,一个`Broker`中一定包含一个完整的的`Virtual Host`,`Exchange`,`Queue`.

- Connection

  ​	`Connection`是由客户端创建的**连接**,但具体的通信在于其上的`Channel`对象,一个具体的`Connection`可以持有多个`Channel`.

- Channel

  ​	**管道**,是`Producer`和`Consumer`与`Broker`间消息来往的具体通道,是在Connection的基础上创建.

-  Virtual Host

  ​        **虚拟主机**,是`Exchange`和`Queue`的父级元素,也可以想象成是`AMQP`协议中的邮局角色,是一个虚拟的结构,可以是一台服务器,也可以是一个集群.

- Routing Key

   	**路由规则**,`Producer`发消息到`Exchange`的时候都会指定一个`Routing Key`,而`Exchange`会通过这个路由规则将消息发送到具体的`Exchange`或者`Queue`.

  ​	

#### AMQP的整体流程

1. `Producer`与`Broker`建立`Connection`之后,再其之上在建立`Channel`对象来进行和`Virtual Host`的通信关系.
2. 此时`Producer`产生`Message`就可以通过`Channel`发送到指定的`Exchange`中.
3. `Exchange`和`Queue`或`Exchange`建立了`Binding`关系,就会在内部维护一张路由表,记录每个对象的`Binding Key`.如果此时接受`Message`会根据其中的`Routing Key`和指定的路由规则进行转发,注意此时如果没有绑定的对`Message`会直接被抛弃.
4. `Queue`在接收到`Message`之后如果么有其连接的`Channel`(也就是没有与其连接的`Consumer`),就会将`Message`持久化保存.如果有就会按顺序发送到`Channel`上.
5. `Consumer`在收到消息之后还要有所谓`ACK`回复,确认消息的签收.`Queue`只有在接收到`ACK`之后才会将这条`Message`删除.当然`Consumer`也可以发送`NACK`此时消息会被重新发送到其他`Channel`.



- 上面仅仅是个人理解,详细可参考下面这篇博文.
- [AMQP协议](https://blog.csdn.net/u012758088/article/details/78024581)
