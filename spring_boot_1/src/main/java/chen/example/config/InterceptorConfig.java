package chen.example.config;

import chen.example.interceptor.VerifyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@Configuration
@EnableWebMvc
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private VerifyInterceptor verifyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verifyInterceptor).addPathPatterns("/**");
    }
}
