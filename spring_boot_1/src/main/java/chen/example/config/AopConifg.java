package chen.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@Configuration
@ComponentScan("chen.example.aop")
@EnableAspectJAutoProxy
public class AopConifg {
}
