package chenbxxx.any;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/5
 */
/**
 * 子类
 */
@Slf4j
public class ReflexDemo{

    private int i = 10;

    private int j = 11;

    private int getI(){
        return this.i;
    }

    private int getJ(){
        return this.j;
    }


    public static void main(String[] args) throws ClassNotFoundException {

        /**
         * 获取类对象(class),每一个类在JVM中只有一个类对象
         *  参数必须为全路径
         *  获取方式：
         *  1、 Class.forName()
         *  2、 ClassName.class();
         *  3、 Objects.getClass(); */
        Class reflex = Class.forName("chenbxxx.any.ReflexDemo");

        // 获取方法
        Method[] methods = reflex.getMethods();
        for(Method method : methods){
            log.info(method.getName());
        }

        // 成员变量
        Field[] declaredFields = reflex.getDeclaredFields();
        for (Field field : declaredFields){
            log.info(field.getName());
        }
    }
}

/**
 * 父类
 */
class Parent{
    private int k = 9;

    private int getK(){
        return this.k;
    }
}

