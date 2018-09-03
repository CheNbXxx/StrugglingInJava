package chenbxxx.demo.concurrent;

import java.util.concurrent.*;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/27
 */
public class ThreadPoolExecutorExample {

    /**
     * 可缓存线程池：
     * 1. 线程数无限制
     * 2. 有空闲线程则复用空闲线程，若无空闲线程则新建线程
     * 3. 一定程度上减少频繁创建/销毁线程，减少系统开销
     */
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    /**
     * 定长线程池
     *  1. 可控制线程最大并发数
     *  2. 超出的线程会在队列中等待
     */
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

    /**
     * 定长线程池：
     * 1. 支持定时及周期性任务执行。
     */
    ExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);

    /**
     * 单线程化的线程池：
     * 1. 有且仅有一个工作线程执行任务
     * 2. 所有任务按照指定顺序执行，即遵循队列的入队出队规则
     */
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public static void main(String[] args) {
        SynchronousQueue<Runnable> synchronousQueue = new SynchronousQueue<>();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100,
                20, TimeUnit.MINUTES, synchronousQueue, r -> new Thread("TestThread"+r));




        for(int i = 0 ; i < 100 ; i++){

        }
    }
}
