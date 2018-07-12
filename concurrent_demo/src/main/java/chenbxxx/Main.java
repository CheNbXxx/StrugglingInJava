package chenbxxx;


import java.security.PrivateKey;
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
        Lock lock = new ReentrantLock();
        MyThread t1 = new MyThread("t1", lock);
        MyThread t2 = new MyThread("t2", lock);
        t1.start();
        t2.start();
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
