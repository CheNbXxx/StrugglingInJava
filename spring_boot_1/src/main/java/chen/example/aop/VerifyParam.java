package chen.example.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@Aspect
@Component
@Slf4j
public class VerifyParam {


    public VerifyParam() {
        log.info("Initialization VerifyParam......");
    }

    // PointCut

    @Pointcut("@annotation(chen.example.annotation.MyVerify)")
    public void pointCut(){}

    @Before("pointCut()")
    public void doBefore(JoinPoint joinPoint){
        log.info("Get into `doBefore` method");

        // 打印请求路径日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(Objects.isNull(attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        log.info("URL          : " + request.getRequestURL().toString());
        log.info("HTTP_METHOD  : " + request.getMethod());
        log.info("IP           : " + request.getRemoteAddr());
        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("ARGS         : " + Arrays.toString(joinPoint.getArgs()));

    }
}
