package chen.example.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@Slf4j
@Component("verifyInterceptor")
public class VerifyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        log.info(" =======>  Entry Interceptor  <=======");
//        log.info("method:{}",request.getMethod());
//        log.info("servletPath:{}",request.getServletPath());
        return true;
    }
}
