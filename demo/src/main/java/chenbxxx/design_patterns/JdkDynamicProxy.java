package chenbxxx.design_patterns;

import java.lang.reflect.*;

/**
 * @author chen
 * @date 2020/6/18 下午11:16
 */

interface Subject{
    void doSomething();
}

class RealSubject implements Subject{

    @Override
    public void doSomething() {
        System.out.println("// realSubject do something");
    }
}

class MyProxy implements InvocationHandler{

    Subject subject;

    public MyProxy(Subject subject) {
        this.subject = subject;
    }

    void before(){
        System.out.println("// ====== before");
    }

    void after(){
        System.out.println("// ====== after");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        method.invoke(subject,args);
        after();
        return null;
    }
}
public class JdkDynamicProxy {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        // 获取代理类
        // JDK动态代理只能代理接口
        final Class<?> proxyClass = Proxy.getProxyClass(Subject.class.getClassLoader(), Subject.class);
        // 获取构造函数
        final Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        // 创建具体的代理对象
        final Subject subject = (Subject) constructor.newInstance(new MyProxy(new RealSubject()));

        subject.doSomething();

        // 另外一种创建形式
//        final Subject subject1 = (Subject) Proxy.newProxyInstance(Subject.class.getClassLoader(), new Class[]{Subject.class}, new MyProxy(new RealSubject()));

    }
}
