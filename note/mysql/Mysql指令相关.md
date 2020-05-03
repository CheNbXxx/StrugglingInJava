



### Mysql指令相关

##### 用户相关

1. 添加用户

```mysql
 // 用户名：chen  密码：19951217  mysql_native_password表示的是`mysql5.x`的加密方式
 CREATE user chen IDENTIFIED [WITH mysql_native_passwprd)] BY '19951217';
```

2.  删除用户
3.  mysqldump -h '118.24.134.237' -uroot -p19951217 --opt --compress db_springboot --skip-lock-tables | mysql -hlocalhost -uroot -p19951217 db_springboot;

```mysql
DROP user chen%'%'
```

3. 密码相关
   1.  修改密码
   ```mysql
   ALTER USER 'chen'@'%' IDENTIFIED [WITH mysql_native_passwprd)] BY 'PASSWORD'； 
   // 密码必须包含大小写加数字
   ```

   2. 密码过期

      ```mysql
      // my.cnf里的全局配置
      [mysqld]
      default_password_lifetime=180
      
      // 动态配置并保存
      SET PERSIST default_password_lifetime=x
      
      // 密码到期时间的账户 NEVER表示永不过期
      CREATE/ALTER USER 'chen'@'localhost' PASSWORD EXPIRE INTERVAL 90 DAY/NEVER/DEFAULT;
      ```

   3. 密码加密方式

      - mysql8.x以上的版本将密码加密方式从`mysql_native_password`改成`caching_sha2_password`

      - 兼容模式 

        ```mysql
        // 改变加密方式
        ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'
        ```

      - my.cnf配置

        ```
        [mysqld]
        default_authentication_plugin=mysql_native_password/caching_sha2_password 
        ```


##### 权限、角色相关

- `mysql8.x`中的新增了角色(role)的概念，说白了就是权限的集合。

1. 创建角色

   ```mysql
   CREATE ROLE 'springboot_read';	  	// 简单创建角色chen
   ```

2. 给角色授权

   ```mysql
   GRANT ALL/SELECT/INSERT/UPDATE/DELETE ON DBnMAE(*).TABLEnAME(*) TO ROLEnAME;
         // 将对于某个数据库，某张表的包括增删改查在内的权限授予给某个权限
   ```

3. 将角色授予账号

   ```mysql
   GRANT ROLEnAME TO USERnAME;	
   ```

4. 查看权限

   ```mysql
	show grants for 'chen';			// 查看用户`chen`的权限
   ```

5. 权限刷新

   ```mysql
    flush privileges
   ```

6. 激活

   ```mysql
   SET default role all to USERNAME;
   ```


##### 建表相关

- 约束

  ```mysql
  // 自动递增，可用`=`指定初始值
  AUTO_INCREMENT（= n）
  // 唯一约束
  UNIQUE KEY
  // 主键约束
  PRIMARY KEY
  ```

- 时间相关   p/s=5qs5sdiN

  ```mysql
  // 新建时默认插入当前时间
  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  // 新建时默认插入当前时间 更新记录是更新时间
  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  ```

-  查询相关

  ```mysql
  SHOW INDEX FROM TABLE_NAME;     // 查看表内索引
  SHOW CREATE TABLE TABLE_NAME;	// 查看建表语句
  SHOW COLUMNS FROM TABLE_NAME;	// 查看表结构
  DESC TABLE_NAME;				// 查看表结构
  SHOW TABLES/SHOW DATABASES;		// 查看表、库
  ```

- 修改相关

  ```mysql
  ALTER TABLE TABLE_NAME ADD/DROP/ALTER/MODIFY/CHANGE/ COLUMN
  ```





##### 存储过程

```mysql
mysql> DELIMITER // 
mysql> CREATE PROCEDURE proc1(OUT s int)    
    -> BEGIN   
    -> SELECT COUNT(*) INTO s FROM user;   
    -> END   
    -> // 
mysql> DELIMITER ;
---------------------
本文来自 wpydaguan 的CSDN 博客 ，全文地址请点击：https://blog.csdn.net/wpydaguan/article/details/40787625?utm_source=copy 
```

- `DELIMITER //` 语义为改变分隔符为`//`，因为`MySQL`会默认将`;`当做分隔符，不改变的话会输入`;`会直接被当成`sql语句`执行，当然声明结束后要改回来。
- `CREATE PROCEDURE` 是存储过程的创建语句，`s`为参数，
  - `IN`为入参，调用时指定
  - `OUT`为出参，也就是返回值
  - `INOUT`即为入参也为出参。
- `BEGIN`和`END` 是存储过程的开始和结束的标识。

- 变量相关
  - 变量定义
    - 局部变量的声明一定要放在存储过程的开始
    - `DECLARE variable_name [,variable_name...] datatype [DEFAULT value];`
    - **eg.** `DECLARE l_int int usigned default 4000000;`
  - 变量赋值
    - `SET variable_name = [值|表达式]`
- 双模杠注释：  `--`

- 指令记录
  - `SHOW PROCESSLIST`
    - 看名称就知道是查看进程列表的指令，可以帮助查找慢sql。
