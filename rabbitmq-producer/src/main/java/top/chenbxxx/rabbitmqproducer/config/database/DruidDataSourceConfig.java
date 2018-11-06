package top.chenbxxx.rabbitmqproducer.config.database;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.chenbxxx.rabbitmqproducer.config.properties.DruidDataSourceProperties;

/**
 *
 * Druid DataSource配置类
 *      注解设定配置类加载,并开始事务支持
 * @note
 *      1. `@EnableConfigurationProperties`: 该注解用于使能上面的注解
 *      3. `@ConditionalOnClass`: IOC条件,检查classpath也可以说是类加载器中是否有对应的类存在
 *      4. `@ConditionalOnProperty`: 讲`name`,`prefix`等字段匹配的属性和`havingValue`比较一致时才生效.`matchIfMissing`表示不存在是是否生效.
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-5
 */
@Configuration
@ConditionalOnClass({DruidDataSource.class})
@ConditionalOnProperty(
        name = {"spring.datasource.type"},
        havingValue = "com.alibaba.druid.pool.DruidDataSource",
        matchIfMissing = true
)
@Slf4j
@EnableConfigurationProperties(DruidDataSourceProperties.class)
public class DruidDataSourceConfig {

    @Autowired
    private DruidDataSourceProperties druidDataSourceProperties;

    @Bean("druidDataSource")
    public DruidDataSource druidDataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setMaxActive(10);
        // 中间可配置Druid相关属性
        return druidDataSource;
    }

}
