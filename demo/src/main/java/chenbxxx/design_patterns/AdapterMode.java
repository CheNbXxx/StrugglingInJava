package chenbxxx.design_patterns;

/**
 * @author chen
 * @date 2020/6/19 上午12:00
 */
public class AdapterMode {
    /**
     * 就是将乘2的方法变成乘4的过程
     */
    interface Target {
        int multiply4(int i);
    }

    class Adaptee {
        int multiply2(int i) {
            return i * 2;
        }
    }

    class Adapter implements Target {

        Adaptee adaptee = new Adaptee();

        @Override
        public int multiply4(int i) {
            return adaptee.multiply2(i) * 2;
        }
    }


}
