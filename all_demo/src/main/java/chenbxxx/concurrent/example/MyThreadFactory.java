package chenbxxx.concurrent.example;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/4
 *
 * 线程工厂类
 */
@Slf4j
public class MyThreadFactory implements ThreadFactory {

    /** 工厂创建线程计数 */
    private int counter;

    /** 线程通用名称 */
    private String currencyName;

    @Override
    public Thread newThread(Runnable r) {
        String threadName =  currencyName+"("+counter+++")";
        log.info("Create thread with name {}",threadName);
        return new Thread(r,threadName);
    }

    public MyThreadFactory(String currencyName){
        // 基底为1
        this.counter = 1;
        this.currencyName = currencyName;
    }

    public int getCounter() {
        return counter;
    }
}
