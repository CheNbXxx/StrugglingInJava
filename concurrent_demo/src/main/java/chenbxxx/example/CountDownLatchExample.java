package chenbxxx.example;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/10
 * @link https://github.com/CheNbXxx/java-demos/wiki/CountDownLatch
 *
 * CountDownLatch的example类
 * 相比于CycliBarrier来说，
 *     CountDownLatch更多的倾向于主从关系，一个主线程等待多个从属线程完成后再执行。
 */
@Slf4j
public class CountDownLatchExample {
    /** 初始化资源为10 */
    private static int RESOURCE = 10;

    /** 共用的计数器 */
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(5);

    private class Producer implements Runnable{
        private CountDownLatch countDownLatch;

        public Producer(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                log.info("{}已经阻塞",Thread.currentThread().getName());
                countDownLatch.await();
                log.info("继续运行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void main() throws InterruptedException {
        for (int i = 0; i < 2 ; i++){
            new Thread(new Producer(COUNT_DOWN_LATCH)).start();
        }

        Thread.sleep(2000);

        COUNT_DOWN_LATCH.countDown();
    }

    public static void main(String[] args) throws InterruptedException {
       new CountDownLatchExample().main();
    }

}
