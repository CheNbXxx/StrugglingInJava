package chenbxxx.example;

import java.util.concurrent.CountDownLatch;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/30
 */
public class CountDownLatchExample {
    private static CountDownLatch countDownLatch = new CountDownLatch(10);
//    private static final ExecutorService executorService = Executors.newCachedThreadPool();


    public static void main(String[] args) {
//        System.out.println(new Random().nextInt(100000,1000000));

    }
}
class CDLRunnable implements Runnable{

    private CountDownLatch countDownLatch;

    @Override
    public void run() {

    }
}
