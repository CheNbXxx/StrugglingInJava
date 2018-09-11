package chen.example.aop;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
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

    /**
     * 拦截MyVerify标注的方法注解
     */
    @Pointcut("@annotation(chen.example.annotation.MyVerify)")
    public void pointCut(){}

    /**
     * .....他娘的 around的执行在Before之前 该方法注释不用
     * @param joinPoint
     */
    @Before("pointCut()")
    public void doBefore(JoinPoint joinPoint){
        log.info("Get into `doBefore` method");

        // 打印请求路径日志
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if(Objects.isNull(attributes)) {
//            return;
//        }
//        HttpServletRequest request = attributes.getRequest();
//        log.info("URL          : " + request.getRequestURL().toString());
//        log.info("HTTP_METHOD  : " + request.getMethod());
//        log.info("IP           : " + request.getRemoteAddr());
//        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
//        log.info("ARGS         : " + Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * 环绕方式
     *   校验的预想步骤：
     *  1. 获取request,具体的校验根据入参上的注解,所以需先知道入参名
     *  2. 获取调用的方法的所有参数,以及参数上注解信息
     *  3. 根据参数上的注解,和request中获取的参数进行校验
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("Get into doAround methodS");

        // 获取HttpServletRequest
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(Objects.isNull(attributes)) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();

        Signature signature = proceedingJoinPoint.getSignature();

        // 获取目标对象
        Class<?> aClass = proceedingJoinPoint.getTarget().getClass();
        // 根据方法名获取对应方法
        Method declaredMethod = aClass.getDeclaredMethod(signature.getName());

        return proceedingJoinPoint.proceed();

    }
}
