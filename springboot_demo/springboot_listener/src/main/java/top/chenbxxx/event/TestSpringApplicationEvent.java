package top.chenbxxx.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.SpringApplicationEvent;

/**
 * 测试用SpringApplicationEvent子类事件
 * @author chenbxxx
 */
public class TestSpringApplicationEvent extends SpringApplicationEvent {
    public TestSpringApplicationEvent(SpringApplication application, String[] args) {
        super(application, args);
    }
}
