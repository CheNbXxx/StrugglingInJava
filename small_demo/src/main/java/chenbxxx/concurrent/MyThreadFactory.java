package chenbxxx.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/4
 * <p>
 * 线程工厂类
 */
@Slf4j
public class MyThreadFactory implements ThreadFactory {

    /**
     * 工厂创建线程计数
     */
    private static int counter = 1;

    /**
     * 线程通用名称
     */
    private String currencyName;

    @Override
    public Thread newThread(Runnable r) {
        String threadName = currencyName + "(" + counter++ + ")";
        return new Thread(r, threadName);
    }

    public MyThreadFactory(String currencyName) {
        this.currencyName = currencyName;
    }

    public int getCounter() {
        return counter;
    }
}
