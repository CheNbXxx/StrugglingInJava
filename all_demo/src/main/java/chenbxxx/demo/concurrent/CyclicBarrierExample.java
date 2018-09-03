package chenbxxx.demo.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/26
 * @content cyclicBarrier可以简单理解为比赛，一个运动员入场还不行，还得等别的运动员全都入场 才能开始比赛。
 */
public class CyclicBarrierExample {

    private static final int PLAYER_NUMBER = 3;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> System.out.println("比赛开始!"));
        for (int i = 0; i < PLAYER_NUMBER; i++) {
            executorService.submit(new Player(String.valueOf(i), cyclicBarrier, reentrantLock));
        }
    }

    static class Player implements Runnable {

        private String threadName;

        private CyclicBarrier cyclicBarrier;

        private ReentrantLock lock;

        public Player(String threadName, CyclicBarrier cyclicBarrier, ReentrantLock lock) {
            this.cyclicBarrier = cyclicBarrier;
            this.threadName = threadName;
            this.lock = lock;
        }

        @Override
        public void run() {
            try {
                System.out.println(threadName + "号球员" + threadName + "到达赛场");
                Thread.sleep(1000);
                cyclicBarrier.await();
                System.out.println("正在球场");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

}

