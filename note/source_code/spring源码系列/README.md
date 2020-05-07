# SpringBoot的源码解析

> 源码基于SpringBoot 2.2.6。

计划如下：

1. SpringBoot的应用启动的总体流程
2. 配置环境加载流程(在SpringBoot启动流程的环境准备阶段)
3. JavaConfig类(@Configuration)/自动化配置原理(@Import)/BeanDefinition的加载流程。
4. 类加载流程，重点有BeanPostProcessor的前后调用以及循环依赖的解决，从启动流程的预加载方法中切入查看。
5. SpringBoot启动过程中的监听器
6. BeanFactoryPostProcessor调用流程
7. BeanPostProcessor调用
8. Aop原理
9. SpringMVC具体流程，重点关注父子容器和请求的调度和处理。
10. SpringBoot的事务支持
11. SpringBoot的异步化支持
12. SpringBoot中@Retry的支持
13. SpringCloud和Spring Data之类的暂定

