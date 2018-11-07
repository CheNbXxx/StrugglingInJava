package chenbxxx.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.*;

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

    private static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),new MyThreadFactory("CountDownLatchExample"));

    /** 随机 */
    private static Random random = new Random(10);

    private abstract class AbstractRunnable implements Runnable{

        /** 线程名 */
        String threadName;

        /** 计数器 */
        CountDownLatch countDownLatch;

        /** 任务所需时间 */
        long needTime ;

        AbstractRunnable(String threadName, CountDownLatch countDownLatch,long needTime) {
            this.threadName = threadName;
            this.countDownLatch = countDownLatch;
            this.needTime = needTime;
        }
    }

    /**
     * 子任务(准备任务的基础线程)
     */
    private class SubTasks extends AbstractRunnable {
        private SubTasks(String threadName,CountDownLatch countDownLatch,long needTime) {
            super(threadName,countDownLatch,needTime);
        }
        @Override
        public void run() {
            try {
                Thread.sleep(needTime);
                log.info("{}准备工作完成",threadName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                countDownLatch.countDown();
            }
        }
    }

    /**
     * 主任务线程
     */
    private class MainTasks extends AbstractRunnable{

        private CountDownLatch myCountDownLatch;

        MainTasks(String threadName, CountDownLatch countDownLatch, long needTime,CountDownLatch myCountDownLatch) {
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
                log.info("经过{}ms，{}主任务执行完成",needTime,threadName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                myCountDownLatch.countDown();
            }
        }
    }

    private void main() throws InterruptedException {
        log.info("|*************  Starting  *************|");
        log.info("共有{}个子线程,{}个主线程",THREAD_SIZE,2);
        for (long i = 0; i < THREAD_SIZE;i++){
            threadPoolExecutor.execute( new SubTasks(i+"号",countDownLatch,random.nextInt(10000)));
        }
        CountDownLatch countDownLatch1 = new CountDownLatch(2);
        for (int i = 0;i < 2;i++){
            threadPoolExecutor.execute(new MainTasks(i+"号",countDownLatch,random.nextInt(10000),countDownLatch1));
        }
        countDownLatch1.await();
        log.info("|************** Ending *************|");
    }


    /**
     * 在5000ms之后调用countDown方法
     * @param countDownLatch    唯一计数器
     * @throws InterruptedException
     */
    private void debugDemo(CountDownLatch countDownLatch) throws InterruptedException {
        Thread.sleep(5000);
        log.info("唤醒主线程");
        countDownLatch.countDown();
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        threadPoolExecutor.execute(() -> {
            try {
                new CountDownLatchExample().debugDemo(countDownLatch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        log.info("主线程开始等待");
        countDownLatch.await();

        new CountDownLatchExample().main();
    }
}
