package top.chenbxxx.rabbitmqproducer.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * `@ConfigurationProperties`: 该注解用于将Properties文件中的属性装配成bean
 *
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-4
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource")
@PropertySource({"classpath:application.properties","classpath:application-private.properties"})
public class DruidDataSourceProperties {

    private String driverClassName;

    private String url;

    private String username;

    private String password;}
