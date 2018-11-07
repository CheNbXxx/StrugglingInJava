package chenbxxx.concurrent;


import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2018/11/7 16:20
 */
public class ThreadPoolExecutorFactory {

    /**
     * 线程池核心线程数目
     */
    private static final int CORE_POOL_SIZE = 5;

    /**
     * 线程池最大线程数
     */
    private static final int MAX_THREAD_SIZE = 10;

    /**
     * 唯一对象
     */
    private volatile static ThreadPoolExecutor instance;

    /**
     * 私有化构造函数
     */
    private ThreadPoolExecutorFactory() {
    }

    ;

    /**
     * 获取唯一线程池
     *
     * @return 线程池实例
     */
    public static ThreadPoolExecutor getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (ThreadPoolExecutorFactory.class) {
                if (Objects.isNull(instance)) {
                    instance = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_THREAD_SIZE, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new MyThreadFactory("测试线程"));
                }
            }
        }
        return instance;
    }
}
