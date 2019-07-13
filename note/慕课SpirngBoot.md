# 小马哥的SpringBoot课程

- 不好意思...朋友那蹭的课程
- 小马哥讲得很快. 笔记有点来不及 只能看完之后总结性的记一下
- 最近写的项目是基于SpringBoot的,所以感觉还是很有必要看一下的.

## SpringBoot的自动装配

#### 手动装配
- 从原始的XML装配到`@Configuration`都算是手动装配。

#### 模式注解

- 一种声明在应用中,扮演`组件`的注解
- 类似`@Companent`,`@Service`,`@Controller`
- 使用`@CompanentScan`装配到应用中
- 具有**层次性**以及**派生性**

#### @Enable注解装配模式

- 将功能含义相近的组件使用`@EnableXXX`来实现统一装配。
- `@EnableXXX`需要标注`@Import`，另外`@Import`的参数中需要包含`ImportSelector`的子类或者`@Configuration`的配置类。
- `@EnableAuthConfiguration`

#### 条件装配

- Bean装配前置判断

##### @Profile
- Spring3.1引入的注解，加载Bean会进行前置判断
- Spring4.0之后，其实现方式改为`@Conditional`

##### @Conditional
- Spring4.0引入的注解，相对来说有更好的弹性

#### 自动装配

1. 激活自动装配 - `@EnableAutoConfiguration`
2. 实现自动装配 -` XXXAutoConfiguration`
3. 配置自动装配的实现 - `META/spring.factories`


## SpringApplication

### SpingApplication准备阶段

#### 配置源
- 不知道咋说。。。

#### 推断应用类型
- 源码位置 - `deduceWebApplicationType`
- `REACTIVE` 和`SERVLET`不能共存,且都存在时会选择`SERVLET`

#### 推断引导类Main Class
- 源码位置 - `deduceMainApplationClass`
- 获取线程堆栈信息，并进一笔获取对应的位置。

#### 加载应用上下文初始器 ApplicationContextInitializer
- 源码位置 - `SpringFactoriesLoader`
- 可根据`@Order`或者实现`Ordered`接口，实现顺序加载
- `spring.factories`配置

#### 加载应用监听
- `spring.factories`配置
- 增加监听需要实现`ApplicationListener`，泛型中指定监听的事件类型


### SpringApplication运行阶段
#### 加载运行运行监听器
- 利用`spring.factories`的工厂加载机制，读取 `SpringApplicationRunListener`对象集合，并封装到组合类`SpringApplicationRunListeners`
- `SpringApplicationRunListener`中声明了已定义的五六种事件

#### 创建应用上下文 ConfigurableApplicationContext
- 根据准备阶段推断的应用类型创建对应的`ConfigurableApplicationContext`实例:
	- Web Reactive : `AnnotationConfigReactiveWebServerApplicationContext`
	- Web Servlet : `AnnotationConfigServletWebServerApplicationContext`
	- 非Web ：`AnnotationConfigApplicationContext`

#### 创建环境 Environment
- 同样是根据应用类型
	- Web Reactive ： `StandardEnvironment`
	- Web Servlet : `StandardServletEnvironment`
	- 非Web ： `StandardEnvironment`
