package chenbxxx.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/3
 * <p>
 * `Semaphore`字面意思就是信号量
 * 用于控制使用或进入共享资源的线程的数量.
 */
@Slf4j
public class SemaphoreExample {
    private static final Semaphore semaphore_5 = new Semaphore(5);

    /**
     * 线程池
     */
    private final static ThreadPoolExecutor threadPoolExecutor;
    /**
     * 线程池容量
     */
    private final static int THREAD_CAPACITY;

    static {
        THREAD_CAPACITY = 5;
        threadPoolExecutor = new ThreadPoolExecutor(THREAD_CAPACITY, THREAD_CAPACITY, 100, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(5), new MyThreadFactory("座位"));
    }

    public SemaphoreExample() throws InterruptedException {
        log.info("|*************  Starting  *************|");
        log.info("情景假设：某餐厅最多有{}个座位供顾客用餐", THREAD_CAPACITY);

        for (int i = 0; i < 100; i++) {
            Thread.sleep(2000L);
            threadPoolExecutor.execute(() -> {
                log.info("有顾客到餐厅,在{}用餐", Thread.currentThread().getName());
                try {
                    semaphore_5.acquire(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                semaphore_5.release(1);
                log.info("{}的顾客用餐结束", Thread.currentThread().getName());
            });
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new SemaphoreExample();
    }
}
