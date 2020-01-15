package top.chenbxxx.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对于自定义的ApplicationListener有两种声明方式:
 * 1. 标记为Bean,不过这会在refresh方法中才创建对象，所以会有部分的事件接收不到
 *      继承ApplicationListener和标注{@link EventListener}效果相同
 * 2. 在spring.factories中声明,会在SpringApplication的构造函数中通过工厂加载模式加载，贯彻应用的全环境，不会错漏信息。
 * 如果都声明的话会初始化两次。
 * @author chenbxxx
 */
@Slf4j
//@Component
//@Scope("singleton")
public class TestApplicationListener implements ApplicationListener<ContextRefreshedEvent>{

    public TestApplicationListener(){
        log.info("test application listener is creating");
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    // 该注解必须配合bean使用。
    // @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("// 第"+atomicInteger.incrementAndGet()+" 次 监听到事件名称： "+event.getClass().getSimpleName());
    }
}
