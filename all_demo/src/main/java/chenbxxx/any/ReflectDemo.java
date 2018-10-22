package chenbxxx.any;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/12
 */
@Slf4j
public class ReflectDemo {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        new ReflectDemo();
    }

    public ReflectDemo() throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        Class<?> aClass = Class.forName("chenbxxx.any.ReflectExample");

        // 获取所有public属性,包括从父类继承的
        log.info(" =====> Fields");
        showField(aClass.getFields());
        // 获取当前类所有成员变量,不包括从父类继承的
        log.info(" =====> DeclaredFields");
        showField(aClass.getDeclaredFields());
        // 获取全部方法 包括从父类继承的
        log.info(" =====> Methods");
        showMethod(aClass.getMethods());
        // 仅获取当前类方法,不包括从父类继承的
        log.info(" =====> DeclaredMethods");
        showMethod(aClass.getDeclaredMethods());



        log.info(" ======> Try to get the specified method ");
        Method method = aClass.getMethod("publicShow", Integer.class, Integer.class);
        log.info(" ======> Try to get annotated parameters");
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters){
            log.info( parameter.getType().getName());
        }
        showType(method.getParameterTypes());
        log.info(" ======> Ending");
    }

    private void showField(Field... fields){
        for (Field field : fields){
            log.info(field.getName());
        }
    }

    private void showType(Type...types){
        for (Type type : types){
            log.info(type.getTypeName());
        }
    }

    private void showMethod(Method...methods){
        for (Method method : methods){
            log.info(" methodName:{}",method.getName());
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters){
                log.info("       param:{},annotation:{}",parameter.getName(),parameter.getAnnotations());
            }
        }
    }
}

@Slf4j
class ReflectExample extends fatherClass{
    public  int public_param;
    private int private_param;
    protected int protected_param;
    int default_param;

    public void publicShow(Integer i, Integer j){
        log.info("public method");
    }

    private void privateShow(int i,int j){
        log.info("private method");
    }

    protected void protectedShow(int i,int j){
        log.info("protected method");
    }
}

@Slf4j
class fatherClass{
    public int k = 10;
    private int j = 10;
    public void fatherMethod(){
        log.info("father method");
    }
}