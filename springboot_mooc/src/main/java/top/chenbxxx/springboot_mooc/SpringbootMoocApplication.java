package top.chenbxxx.springboot_mooc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import top.chenbxxx.springboot_mooc.annotation.ConditionalOnSystemProperty;

@SpringBootApplication
public class SpringbootMoocApplication {


    @Bean
    @ConditionalOnSystemProperty(name = "user.name",value = "chen")
    public String chenbxxx(){
        return "chenbxxx";
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext run = new SpringApplicationBuilder(SpringbootMoocApplication.class)
                .web(WebApplicationType.NONE)
                .run();

        System.out.println(run.getBean("chenbxxx"));
    }

}
