package chen.example.annotation;

import java.lang.annotation.*;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 *
 * 参数验证的注解,使用该注解的原因是为了AOP统一拦截
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER,ElementType.ANNOTATION_TYPE})
@Documented
public @interface MyVerify {
    String value() default "";
}
