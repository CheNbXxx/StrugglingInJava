package chenbxxx.design_patterns;

import lombok.Synchronized;

/**
 * @author chen
 * @date 19-3-30
 */
public class Singleton {
    /**
     * 饿汉模式
     * 优点:
     * 1. 线程安全,类加载时就创建了唯一对象
     * 缺点:
     * 1. 无法传递创建参数
     * 2. 若不使用会存在内存浪费
     */
    static class HungryMode {
        /**
         * 类加载时就创建了唯一对象实例
         */
        private static HungryMode instance = new HungryMode();

        /**
         * 私有化构造函数,外部无法通过构造函数构建对象
         */
        private HungryMode() {
        }

        /**
         * 对外提供唯一实例的获取方法
         *
         * @return 实例对象
         */
        public static HungryMode getInstance() {
            return instance;
        }
    }

    /**
     * 懒汉模式
     * 优点:
     * 1. 懒加载,不会造成内存的浪费
     * 缺点:
     * 1. 存在线程安全问题(因为对象是在getInstance方法中调用的,多线程访问时可能同时创建多个对象)
     */
    static class LazyMode {

        private static LazyMode instance;

        private LazyMode() {
        }

        /**
         * 在获取实例的方法中,实现懒加载(在调用时才会创建对象实例)
         *
         * @return 唯一实例
         */
        public static LazyMode getInstance() {
            if (instance == null) {
                instance = new LazyMode();
            }
            return instance;
        }
    }

    /**
     * 静态内部类模式
     *   区别于饿汉和懒汉模式,实例保存在内部类中.
     * 优点:
     *   1. 懒加载 不会在类加载时就被加载进内存,而是在获取是加载.
     *   2. 类加载的过程都是线程安全的.
     */
    static class InnerClassMode{
        /**
         * 使用内部类保存唯一实例,外部类加载时并不会加载实例,而是在获取时加载保证了
         */
        private static class SingletonHandle{
            private static final InnerClassMode instance = new InnerClassMode();
        }

        private static InnerClassMode getInstance(){
            return SingletonHandle.instance;
        }
    }

    /**
     * 枚举模式:
     * 借由枚举类的特性,
     * 既能避免序列化问题,又能保证线程安全问题
     */
    enum EnumMode{
        /**
         * 唯一实例对象
         */
        INSTANCE;

        public static EnumMode getInstance(){
            return INSTANCE;
        }
    }

    /**
     * 双重校验锁模式
     * 实现了懒加载,为了保证线程安全(实例的唯一)需要加volatile以及双重校验
     * 性能并不是很好
     */
    static class DoubleLockMode{
        /**
         * volatile - 保证唯一实例的可见性
         * 在创建了但并未赋值给instance对象时,如果另外一个线程同时检测到会重新创建一个实例对象
         */
        private static volatile DoubleLockMode instance;

        private DoubleLockMode(){}

        public static DoubleLockMode getInstance(){
            if(instance == null){
                synchronized (DoubleLockMode.class){
                    if(instance == null){
                        instance =  new DoubleLockMode();
                    }
                }
            }
            return instance;
        }
    }


}
