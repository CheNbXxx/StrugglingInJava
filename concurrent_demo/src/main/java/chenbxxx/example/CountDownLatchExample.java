package chenbxxx.example;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
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
    private static final int THREAD_SIZE = 4;

    /** 共用的计数器 */
    private static CountDownLatch countDownLatch = new CountDownLatch(THREAD_SIZE);

    /** 随机 */
    private static Random random = new Random(10);

    private abstract class MyRunnable implements Runnable{

        /** 线程名 */
        String threadName;

        /** 计数器 */
        CountDownLatch countDownLatch;

        /** 任务所需时间 */
        long needTime ;

        MyRunnable(String threadName, CountDownLatch countDownLatch,long needTime) {
            this.threadName = threadName;
            this.countDownLatch = countDownLatch;
            this.needTime = needTime;
        }
    }

    /**
     * 子任务(准备任务的基础线程)
     */
    private class Subtasks extends MyRunnable {
        private Subtasks(String threadName,CountDownLatch countDownLatch,long needTime) {
            super(threadName,countDownLatch,needTime);
        }
        @Override
        public void run() {
            try {
                Thread.sleep(needTime);
                log.info("{}准备工作完成",threadName);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 主任务线程
     */
    private class Maintasks extends MyRunnable{

        private CountDownLatch myCountDownLatch;

        Maintasks(String threadName, CountDownLatch countDownLatch, long needTime,CountDownLatch myCountDownLatch) {
            super(threadName, countDownLatch, needTime);
            this.myCountDownLatch = myCountDownLatch;
        }

        @Override
        public void run() {
            try {
                log.info("{}主任务正在等待",threadName);
                countDownLatch.await();
                log.info("{}主任务开始执行",threadName);
                Thread.sleep(needTime);
                log.info("{}主任务执行完成",threadName);
                myCountDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void main() throws InterruptedException {
        log.info("|*************  Starting  *************|");
        for (long i = 0; i < THREAD_SIZE;i++){
            new Thread(new Subtasks(i+"号",countDownLatch,random.nextInt(1000))).start();
        }

        CountDownLatch countDownLatch1 = new CountDownLatch(2);

        for (int i = 0;i < 2;i++){
            new Thread(new Maintasks(i+"号",countDownLatch,random.nextInt(1000),countDownLatch1)).start();
        }

        countDownLatch1.await();
        log.info("|************** Ending *************|");
    }

    public static void main(String[] args) throws InterruptedException {
       new CountDownLatchExample().main();
    }
}
