package chenbxxx.example;

import java.util.concurrent.CountDownLatch;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/17
 */
public class CountDownLatchExample {
//    CountDownLatch

    public static void main(String[] args) {
        final  CountDownLatch countDownLatch = new CountDownLatch(3);


        for (int i = 0;i < 2;i++){
            new Thread(() -> {
                System.out.println("子线程:"+Thread.currentThread().getName()+"正在执行");
                // 模仿执行过程
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("子线程:"+Thread.currentThread().getName()+"执行完毕");
                countDownLatch.countDown();
            }).start();

        }

        try {
            System.out.println("主线程等待中//////");
            countDownLatch.await();
            System.out.println("主线程开始执行");
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

