package chenbxxx;

import java.util.concurrent.TimeUnit;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2018/11/8 10:55
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                new ThreadLocal().get();
                System.out.println("调用了Get1");
            }
        });
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                new ThreadLocal<>().get();
                System.out.println("调用了Get12");
            }
        });


        thread.start();
        thread1.start();


        TimeUnit.SECONDS.sleep(10);

        System.out.println("11");
    }
}

