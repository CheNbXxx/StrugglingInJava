package chenbxxx.example;

import javax.management.relation.RoleUnresolved;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/3
 */
public class LocksupportExample {

    public void parkThis(){
        System.out.println("|************ park主线程 ************|");
        /**
         * 妈了个臭嗨，还以为传个对象进去会阻塞所有调用该对象的线程！！！！假的
         */
        LockSupport.park(this);
    }

    public void testPark(){
        System.out.println("|************* 对象相关线程未被阻塞 *************|");
    }

    public static void main(String[] args) {
        LocksupportExample locksupportExample = new LocksupportExample();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(new LockRunnable(Thread.currentThread(),countDownLatch)).start();
        new Thread(new RunnableTest(locksupportExample,countDownLatch)).start();

        locksupportExample.parkThis();
        System.out.println("after park");
    }
}

class RunnableTest implements Runnable{

    LocksupportExample locksupportExample;

    CountDownLatch countDownLatch;

    public RunnableTest(LocksupportExample locksupportExample, CountDownLatch countDownLatch) {
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

class LockRunnable implements Runnable{

    Thread currThread;
    CountDownLatch countDownLatch;

    public LockRunnable(Thread thread,CountDownLatch countDownLatch){
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
