package top.chenbxxx.rabbitmq.config.database;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @note
 *    1. `@EnableTransactionManagement`: 该注解用于开启事务支持
 *    2. `@AutoConfigureAfter`: 表示本类表示的bean的装配在value所代表的类装配完成后.
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-5
 */
@Configuration
@EnableTransactionManagement
@Slf4j
@AutoConfigureAfter(DruidDataSourceConfig.class)
public class MyBatisConfig {
    @Autowired
    private DataSource druidDataSource;

    @Bean("sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        // 设置数据源
        sessionFactory.setDataSource(druidDataSource);
        // 设置别名包
        sessionFactory.setTypeAliasesPackage("top.chenbxxx.rabbitmq.entity");
        // 设置Mapper地址
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapping/*.xml"));
        return sessionFactory.getObject();
    }

    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(druidDataSource);
    }
}
