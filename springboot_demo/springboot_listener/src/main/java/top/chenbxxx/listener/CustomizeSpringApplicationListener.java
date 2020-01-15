package top.chenbxxx.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import top.chenbxxx.event.TestSpringApplicationEvent;

/**
 * @author chenbxxx
 */
@Slf4j
@Component
public class CustomizeSpringApplicationListener implements ApplicationListener<TestSpringApplicationEvent> {
    public void onApplicationEvent(TestSpringApplicationEvent event) {
        log.info("嗅探到TestSpringApplicationEvent,className: {},timestamp :{}",event.getClass().getSimpleName(),event.getTimestamp());
    }
}
