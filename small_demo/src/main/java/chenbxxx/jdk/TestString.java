package chenbxxx.jdk;

/**
 * String对象测试类
 *      首先我们要知道 `=`是对比地址，或者说引用地址。
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-17
 */
public class TestString {
    public static void main(String[] args) {
        /**
         * 首先我们知道String在Java中被设计成了不可变类,另外String也会被存入常量池中
         */

        // JVM会在以字面量形式创建字符串对象时，现在常量池中检查是否存在该对象，
        // 如果不存在则在常量池中创建该字符串并返回其引用
        String s1 = "CheNbXxx";
        String s2 = "CheNbXxx";
        // true
        System.out.println(s1 == s2);

        // 如果以new形式创建字符串时，JVM并不会检查常量池中是否存在
        // 是直接在堆中创建该对象
        String s3 = new String("CheNbXxx");
        String s4 = new String("CheNbXxx");
        // false
        System.out.println(s3 == s4);

        // String.intern()
        // 通过下面两行输出可以看出intern()方法会在创建后修改引用的地址，
        // 如果在常量池中有指定的string对象，会直接返回该引用地址
        // 也就是说此时s4.intern()指向的是常量池，而s3依旧指向堆中的字符串对象(注意此时的s4并未改变)
        // false
        System.out.println(s3 == s4.intern());
        // true
        System.out.println(s1 == s4.intern());
        // false
        System.out.println(s1 == s4);


        // 只要全部是以字面量形式创建的字符串，全部都会到常量池中检查是否存在
        String s5 = "CheN"+"bXxx";
        // true
        System.out.println(s5 == s1);

        // 表达式中存在非字面量形式的创建就会在堆中创建对象
        String tempS = "bXxx";
        String s6 = "CheN"+new String("bXxx");
        String s7 = "CheN"+tempS;
        // false
        System.out.println(s5 == s6);
        // false
        System.out.println(s7 == s5);

        int[] i = new int[10];
        System.out.println(i);
    }
}
