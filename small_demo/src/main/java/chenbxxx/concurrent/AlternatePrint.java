package chenbxxx.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author CheNbXxx
 * @description
 *      以notify/wait/synchronized形式的线程间通信控制交替输出.
 *      注意: wait/notify/notifyAll都必须在获取到锁之后才能用，就是在Synchronized块中。
 * @email chenbxxx@gmail.con
 * @date 2018/11/6 16:55
 */
@Slf4j
public class AlternatePrint {

    private static int PRINT_SIGN = 0;

    private static int threadCount = 0;

    private static final ExecutorService executorService = new ThreadPoolExecutor(2,2,10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),new TempThreadFactory());

    public static void main(String[] args) throws InterruptedException {
        Object lock1 = new Object();
        Object lock2 = new Object();

        executorService.execute(new Runnable1(lock1,lock2));

        // 保证线程执行的先后顺序.... 写注释的时候发现好像不用也可以的啊
        TimeUnit.SECONDS.sleep(10);

        executorService.execute(new Runnable1(lock2,lock1));
    }

    /**
     * 输出方法，属于线程间竞争的资源
     */
    private static void print(){
        // 虽然log已经有线程名字了。。但我还是想输出一下
        log.info("/*****************"+Thread.currentThread().getName()+":"+ (++PRINT_SIGN) +"***************/");
    }

    /**
     * 线程工程类，方便日志查看
     */
    static class TempThreadFactory implements ThreadFactory{

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"第"+ (++threadCount) + "个线程");
        }
    }

    static class Runnable1 implements Runnable{

        private final Object lock;

        private final Object otherLock;

        Runnable1(Object lock, Object otherLock){
            this.lock = lock;
            this.otherLock = otherLock;
        }

        @Override
        public void run() {
            while (true) {
                // 拖慢节奏，看清输出效果
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                // 获取到lock的锁
                synchronized (lock) {
                    // 获取到otherLock的锁
                    synchronized (otherLock) {
                        // 唤醒
                        otherLock.notifyAll();
                        print();
                    }
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
