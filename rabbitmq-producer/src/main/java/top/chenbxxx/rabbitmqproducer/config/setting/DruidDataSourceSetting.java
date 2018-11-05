package top.chenbxxx.rabbitmqproducer.config.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-4
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
@PropertySource("classpath:")
public class DruidDataSourceSetting {

    private String driverClassName;

    private String url;

    private String username;

    private String password;}
