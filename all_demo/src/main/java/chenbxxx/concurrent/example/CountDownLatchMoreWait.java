package chenbxxx.concurrent.example;

import chenbxxx.example.HashExample;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-11
 */
@Slf4j
public class CountDownLatchMoreWait extends HashExample {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new moreWaitRunnable("测试线程1",countDownLatch));
        executorService.submit(new moreWaitRunnable("测试线程2",countDownLatch));

        TimeUnit.SECONDS.sleep(10);
        System.out.println("======>调用countDown方法");
        countDownLatch.countDown();

    }

}



@Slf4j
class moreWaitRunnable implements Runnable{

    private String threadName;

    private CountDownLatch countDownLatch;

    public moreWaitRunnable(String threadName,CountDownLatch countDownLatch) {
        this.threadName = threadName;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        log.info("======>:{}线程启动",threadName);
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("线程已被唤醒,总共等待{}s",Duration.between(LocalDateTime.now(),localDateTime).getSeconds());
    }
}

