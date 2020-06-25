package chenbxxx.design_patterns;

/**
 * @author chen
 * @date 2020/6/22 下午10:42
 */
public class StrategyMode {
    public enum Strategy {
        ADD {
            @Override
            public int calculate(int a, int b) {
                return a + b;
            }
        },
        REDUCE {
            @Override
            public int calculate(int a, int b) {
                return a - b;
            }
        };

        public abstract int calculate(int a, int b);
    }

    public static void main(String[] args) {
        System.out.println(Strategy.ADD.calculate(1, 2));
        System.out.println(Strategy.REDUCE.calculate(1, 2));
    }
}
