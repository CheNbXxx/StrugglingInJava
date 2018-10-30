package top.chenbxxx.webdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.chenbxxx.webdemo.interceptor.Interceptor1;
import top.chenbxxx.webdemo.interceptor.InterceptorTest;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InterceptorTest()).order(2).addPathPatterns("/**");
        registry.addInterceptor(new Interceptor1()).order(1).addPathPatterns("/**");
    }
}
