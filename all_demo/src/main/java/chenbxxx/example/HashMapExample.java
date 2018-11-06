package chenbxxx.example;

import java.util.HashMap;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/27
 */
public class HashMapExample {
    public static void main(String[] args) throws ClassNotFoundException {
//        System.out.println(HashMapExample.tableSizeFor(4));
//        System.out.println(3 ^ 5);
//
//        System.out.println(Integer.toBinaryString(-8));
//        System.out.println(Integer.toBinaryString(-8 >>> 1));
//
//        System.out.println(Integer.toBinaryString(8));
//
//        System.out.println(Integer.toBinaryString(8 >> 1));

        Class clazz = HashMap.class;
//        Type[] genericInterfaces = clazz.getGenericInterfaces();
//
//        System.out.println("/******************  genericInterfaces  ******************/");
//        for (Type type : genericInterfaces){
//            System.out.print(type.getTypeName());
//            if(type instanceof ParameterizedType){
//                System.out.print(" instanceof ParameterizedType");
//            }
//            System.out.println("");
//        }

        Class<TestClass> testClassClass = TestClass.class;
        Class.forName("chenbxxx.example.TestClass");

    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        System.out.println(n);
        n |= n >>> 2;
        System.out.println(n);
        n |= n >>> 4;
        System.out.println(n);
        n |= n >>> 8;
        System.out.println(n);
        n |= n >>> 16;
        System.out.println(n);
        return n+1;
    }
    public void showName(){
        System.out.println(this.getClass().getSimpleName());
    }
}

class TestClass{
    static {
        System.out.println("TestClass static code");
}

    public TestClass(){
        System.out.println("Test Constructor code");
    }
}