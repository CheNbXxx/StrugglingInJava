package chen.example.aop;

import chen.example.annotation.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 *
 *        本来想做一个页面和Controller层的参数校验,由于各种转换障碍和反射的不熟悉...暂且搁置
 */
//@Aspect
//@Component
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
     */
    @Before("pointCut()")
    public void doBefore(){
        log.info("Get into `doBefore` method");
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

        // 方法签名 ===> Method
        Signature signature = proceedingJoinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature;
        Method method = methodSignature.getMethod();

        Method realMethod = proceedingJoinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(), method.getParameterTypes());

        // method ===> Parameter
        Parameter[] parameters = realMethod.getParameters();
        log.info("parameters:{}", Arrays.toString(parameters));

        // 无参数则放行
        if(parameters.length <= 0){
            return proceedingJoinPoint.proceed();
        }

        boolean sign = true;
        String message = "";
        for (Parameter parameter : parameters){
            log.info("正在获取名字为:{}的参数",parameter.getName());
            // 获取校验参数的入参值
            Object attribute = request.getAttribute(parameter.getName());
            log.info("校验参数{},值为{}",parameter.getName(),attribute.toString()
            );
            Annotation[] annotations = parameter.getAnnotations();
            for (Annotation annotation : annotations){
                if((message = doVerify(attribute,annotation)).equals("success")){
                    sign = false;
                    break;
                }
            }
        }

        if(!sign){
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            JSONObject result = new JSONObject();
            result.put("code",1);
            result.put("message",message);

            PrintWriter writer = response.getWriter();
            writer.print(result.toString());
            writer.close();
            response.flushBuffer();
            return null;
        }

        return proceedingJoinPoint.proceed();
    }


    /**
     * 验证方法
     * @param attribute     属性值
     * @param annotation    注解名称
     * @return
     */
    private String doVerify(Object attribute,Annotation annotation){
        if(annotation instanceof NotNull) {
            if(Objects.isNull(attribute)){
                return ((NotNull) annotation).message();
            }
        }
        return "success";
    }

}
