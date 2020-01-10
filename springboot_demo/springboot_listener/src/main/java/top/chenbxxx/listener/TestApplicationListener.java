package top.chenbxxx.listener;

import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.EventListener;

/**
 * @author chenbxxx
 */
@Component
public class TestApplicationListener implements ApplicationListener<ApplicationEvent> {
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("// ======================== 监听到事件名称： "+event.getClass().getSimpleName());
    }
}
