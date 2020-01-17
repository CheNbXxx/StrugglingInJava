package top.chenbxxx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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

        List<ApplicationContextInitializer> applicationContextInitializers = SpringFactoriesLoader.loadFactories(ApplicationContextInitializer.class, ListenerBootStrap.class.getClassLoader());

        System.out.println(applicationContextInitializers);

    }
}
