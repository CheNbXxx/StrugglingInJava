package top.chenbxxx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import top.chenbxxx.event.TestApplicationContextEvent;
import top.chenbxxx.event.TestSpringApplicationEvent;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chenbxxx
 */
@SpringBootApplication
public class ListenerBootStrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ListenerBootStrap.class, args);
        run.publishEvent(new TestApplicationContextEvent(run));
    }
}
