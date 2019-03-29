package chenbxxx.example.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/3
 */
public class LockSupportExample {

    /**
     * `park`测试的具体方法
     */
    private void parkThis() {
        System.out.println("|************ park主线程 ************|");
        /**
         * 妈了个臭嗨，还以为传个对象进去会阻塞所有调用该对象的线程！！！！假的
         */
        LockSupport.park(this);
    }

    private void testPark() {
        System.out.println("|************* 对象相关线程未被阻塞 *************|");
    }

    public static void main(String[] args) {
        LockSupportExample locksupportExample = new LockSupportExample();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        ThreadPoolExecutorFactory.getInstance().execute(new LockRunnable(Thread.currentThread(), countDownLatch));
        ThreadPoolExecutorFactory.getInstance().execute(new RunnableTest(locksupportExample, countDownLatch));

        locksupportExample.parkThis();
        System.out.println("after park");
    }

    static class RunnableTest implements Runnable {

        LockSupportExample locksupportExample;

        CountDownLatch countDownLatch;

        RunnableTest(LockSupportExample locksupportExample, CountDownLatch countDownLatch) {
            this.locksupportExample = locksupportExample;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            locksupportExample.testPark();
            System.out.println("|********* 清空countDownLatch,唤醒LockRunnable的线程 ********|");
            countDownLatch.countDown();
        }
    }

    static class LockRunnable implements Runnable {

        Thread currThread;
        CountDownLatch countDownLatch;

        LockRunnable(Thread thread, CountDownLatch countDownLatch) {
            this.currThread = thread;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                System.out.println("|********* 开始等待countDown ***********|");
                countDownLatch.await();
                System.out.println("|************ 5s后unpark主线程 ************|");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("|************ unpark主线程 ************|");
            LockSupport.unpark(currThread);

        }
    }
}

