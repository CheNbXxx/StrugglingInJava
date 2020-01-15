package top.chenbxxx.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Component;
import top.chenbxxx.event.TestApplicationContextEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenbxxx
 */
@Slf4j
@Component
public class CustomizeApplicationContextListener implements ApplicationListener<TestApplicationContextEvent> {
    AtomicInteger atomicInteger = new AtomicInteger();
    public void onApplicationEvent(TestApplicationContextEvent event) {
        log.info("// =========================  {}次,接收到一个ApplicationContextEvent ,timestamp:[{}]",atomicInteger.incrementAndGet(),event.getTimestamp());
    }
}
