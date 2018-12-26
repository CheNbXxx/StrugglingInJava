package top.chen.spring_demo.pojo;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author chen
 *      通过类获取配置文件中的配置属性
 * @description
 * @email ai654778@vip.qq.com
 * @date 18-12-23
 */
@Data
@ToString
@Component
@PropertySource("classpath:application-customize.yml")
@ConfigurationProperties(prefix = "chen")
public class CustomizeProperty {
    private String name;


}
