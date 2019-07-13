# Mongo

- 因为下周的需求里面需要用到Mongo相关的知识，所以开始接触学习 - 20190630

## Mongo基础

- Mongo的默认端口为`27017`

- Mongo存储的格式是BSON，BSON是一种二进制序列化格式。

- 每种BSON都有Integer和String两种表示

- `ObjectId`是Mongo默认为每个文档生成的唯一标识，具有唯一性，能快速生成，排序并且小，每个`ObjectId`大小都是12字节

  - 前四个字节是Unix的时间戳，中间五个字节是随机数，后三个字节是随机数的统计个数

- 与MySQL等传统的关系型数据库的概念对比

  | RDBMS                    | Mongo            |
  | ------------------------ | ---------------- |
  | database(数据库)         | database(数据库) |
  | table(表)                | collection(集合) |
  | row(数据行,表里面的数据) | document(文档)   |
  | column(字段)             | filed(属性)      |

### 基础命令

```shell
// 连接远程Mongo服务器的命令
- mongo ipAddress
// mongo username:password@ipAdress/dbname

- show dbs  -  查看所有数据库
- show collections  -  查看所有集合
- use dbName  -  切换到dbName的数据库，不存在就自动创建
- db.dropDatabase()  -  删除当前数据库

- db.collection.remove()  -  清空collection数据库

- db.createCollection(name,options)  -  创建集合
// options 包含如下参数
// capped 为true是表示固定大小的集合,数据超过阈值会覆盖，true时必须指定size
// autoIndexId 默认为false,true时表示自动在_id字段创建索引
// max 指定固定的集合中文档的最大数量
// size 表示字节记的最大值

- db.collection.drop()  -  删除当前集合
- db.collection.insertMony() | db.collection.insertOne()
// 一次插入多条|单条文档到集合
// collection可以替换为指定的集合,否则为当前集合

- db.collection.find(query,projection)  -  查询所有文档
// collection可指定集合
// query 执行查询条件,<field>:<value>形式 
// db.collection.find({status:"D"})
// projection 投影操作返回指定的建
// db.collection().find().pretty()  -  以更易读的方式读取数据
// db.collection().findOne()    -   只返回一个文档
// db.collection.find({size:{h:14,w:21}})   -  嵌入式文档匹配
// db.collectiom.find({"size.h":14})
- db.collection.update()

```

