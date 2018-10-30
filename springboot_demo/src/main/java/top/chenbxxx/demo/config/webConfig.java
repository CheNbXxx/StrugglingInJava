package top.chenbxxx.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *  Web相关的配置信息,WebMvcConfigurerAdapter被标记为过时,所以此处使用WebMvcConfigurer.
 *  需要配置什么直接Override对应的方法就好
 */
@Configuration
public class webConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }
}
