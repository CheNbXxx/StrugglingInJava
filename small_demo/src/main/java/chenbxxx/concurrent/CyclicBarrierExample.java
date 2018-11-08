package chenbxxx.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/26
 * <p>
 * cyclicBarrier可以简单理解为比赛，一个运动员入场还不行，还得等别的运动员全都入场 才能开始比赛。
 */
public class CyclicBarrierExample {

    /**
     * 运动员需求数
     */
    private static final int PLAYER_NUMBER = 3;

    /**
     * 公用线程池
     */
    private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new MyThreadFactory("CyclicBarrierExample"));

    public static void main(String[] args) throws InterruptedException {
        // 回调函数用的匿名内部类，所以不能用log.info方法打印
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> System.out.println("比赛开始!"));
        for (int i = 0; i < PLAYER_NUMBER; i++) {
            executorService.execute(new Player(String.valueOf(i), cyclicBarrier));
            // 休眠10s 为了效果突出
            TimeUnit.SECONDS.sleep(10);
        }
    }

    /**
     * 运动员类
     */
    @Slf4j
    static class Player implements Runnable {

        /**
         * 线程名字
         */
        private String playerNum;

        /**
         * 公用栅栏
         */
        private CyclicBarrier cyclicBarrier;

        Player(String playerNum, CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
            this.playerNum = playerNum;
        }

        @Override
        public void run() {
            try {
                log.info(playerNum + "号球员" + playerNum + "即将到达赛场");

                // 休眠20s
                TimeUnit.SECONDS.sleep(20);

                log.info(playerNum + "号球员" + playerNum + "到达赛场");
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

}

