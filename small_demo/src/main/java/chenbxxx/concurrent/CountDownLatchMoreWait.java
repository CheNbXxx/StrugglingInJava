package chenbxxx.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * @author chen
 * @description
 *     本类为`CountDownLatch`多等多测试类
 * @email ai654778@vip.qq.com
 * @date 18-10-11
 */
@Slf4j
public class CountDownLatchMoreWait {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new MoreWaitRunnable("测试线程1",countDownLatch));
        executorService.submit(new MoreWaitRunnable("测试线程2",countDownLatch));

        TimeUnit.SECONDS.sleep(10);
        System.out.println("======>调用countDown方法");
        countDownLatch.countDown();

    }

}



@Slf4j
class MoreWaitRunnable implements Runnable{

    private String threadName;

    private CountDownLatch countDownLatch;

    MoreWaitRunnable(String threadName, CountDownLatch countDownLatch) {
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
        log.info("线程已被唤醒,总共等待{}s",Math.abs(Duration.between(LocalDateTime.now(),localDateTime).getSeconds()));
    }
}

