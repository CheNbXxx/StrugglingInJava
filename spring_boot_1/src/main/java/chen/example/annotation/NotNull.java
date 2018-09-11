package chen.example.annotation;

import java.lang.annotation.*;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@MyVerify
public @interface NotNull{
}
