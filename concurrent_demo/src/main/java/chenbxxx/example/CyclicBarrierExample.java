package chenbxxx.example;

import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/26
 */
public class CyclicBarrierExample {

    private static final int THREAD_SIZE = 10;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        for (int i = 0;i < THREAD_SIZE; i++){
            executorService.submit(new CBRunnable("线程"+i,cyclicBarrier,reentrantLock));
        }
    }

}
class CBRunnable implements Runnable{

    private String threadName;

    private CyclicBarrier cyclicBarrier;

    private ReentrantLock lock;

    public CBRunnable(String threadName,CyclicBarrier cyclicBarrier,ReentrantLock lock) {
        this.cyclicBarrier = cyclicBarrier;
        this.threadName = threadName;
        this.lock = lock;
    }

    @Override
    public void run() {
//        synchronized (cyclicBarrier) {
            try {
                System.out.println(threadName + "开始执行run()方法");
                Thread.sleep(1000);
                cyclicBarrier.await();
                System.out.println(threadName + "继续执行run()方法");

            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
    }

//    }
}
