package chenbxxx.jdk;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-9-16
 */
public class ExecutionOrder {
    public static void main(String[] args){

        System.out.println("|**************  Create Maps Object ************|");

        /**
         * 代码调用的顺序：
         * 1. 父类的静态代码块(多个按声明的顺序)
         * 2. 当前类静态代码块(多个顺序同上)
         * 3. 父类的非静态初始代码块
         * 4. 父类的构造函数
         * 5. 当前类非静态代码块
         * 6. 当前类的构造函数
         */
        Maps maps = new Maps();

        System.out.println("|************** Create Map Object **************|");

        /**
         * 调用发现：
         * 1. `静态代码块`全局只执行一次，优先于任何本类方法
         * 2. `非静态初始代码块`每次都会执行,且优先于`构造函数`
         */
//        Map map = new Map();

//        Maps.call();

   }
}

/**
 * 测试调用顺序的具体类
 *  三个方法分别为：
 *      1. 静态代码快
 *      2. 非静态初始代码块
 *      3. 构造函数
 */
class Map{

    static {
        System.out.println("Map Static Code Block");
    }

    {
        System.out.println("Map Not Static Code Block");
    }

    Map(){
        System.out.println("map Constructor Method");
    }


}

class Maps extends Map{

    Maps(){
        System.out.println("Maps Contructor Method");
    }

    static{
        System.out.println("Maps Static Code Block");
    }

    {
        System.out.println("Maps Not Static Code Block");
    }

    static void call(){
        System.out.println("HelloWorld");
    }
}

