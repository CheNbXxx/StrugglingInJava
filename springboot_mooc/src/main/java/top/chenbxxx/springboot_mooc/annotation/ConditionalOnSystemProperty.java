package top.chenbxxx.springboot_mooc.annotation;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * @author chen
 * @date 19-6-23 下午5:13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Documented
@Configuration()
public @interface ConditionalOnSystemProperty {

    String name();

    String value();
}
