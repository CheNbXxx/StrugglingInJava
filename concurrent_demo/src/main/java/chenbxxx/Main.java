package chenbxxx;


import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/10
 */
public class Main {

    private Thread consumer;
    private Thread producer;

    public Main(Runnable consumer, Runnable producer) {
        consumer = new Thread(consumer);
        producer = new Thread(producer);
    }

    public void run(){
        consumer.run();
        producer.run();
    }

    public static void main(String[] args) throws InterruptedException {
//        show(Arrays.asList(1,2,3,4,5,5))
// ;
        // 接单时间
        LocalDateTime checkTime = LocalDateTime.now().minusMinutes(11).minusSeconds(20);
        // 当前时间
        LocalDateTime srcTime = LocalDateTime.now();

        System.out.println(srcTime.until(checkTime,ChronoUnit.SECONDS));
        Duration duration = Duration.between(checkTime, srcTime);
        Long minutes = duration.toMinutes();
        System.out.println(minutes);
    }
    private static void show(List<Integer> ints){
        for (Integer i : ints){
            System.out.println(i);
        }
    }
}
class MyThread extends Thread {
    private Lock lock;
    public MyThread(String name, Lock lock) {
        super(name);
        this.lock = lock;
    }

    @Override
    public void run () {
        lock.lock();
        try {
            System.out.println(Thread.currentThread() + " running");
        } finally {
            lock.unlock();
        }
    }
}
