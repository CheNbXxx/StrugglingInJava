package chenbxxx.example;

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
public class CountDownLatchExample {
    /** 初始化资源为10 */
    private static int RESOURCE = 10;

    /** 共用的计数器 */
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(10);

    private class Producer implements Runnable{

        private CountDownLatch countDownLatch;

        public Producer(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {

        }
    }

}
