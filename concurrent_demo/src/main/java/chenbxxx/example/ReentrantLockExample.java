package chenbxxx.example;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/17
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 以ReetrantLock
 */
public class ReentrantLockExample {
    /** 声明锁 */
    private Lock lock;
    /** 容量 */
    private int capacity;
    /** 实际大小 */
    private int size;
    /** 生产条件 */
    private Condition fullCondition;
    /** 消费条件 */
    private Condition emptyCondition;

    public ReentrantLockExample(int capacity) {
        this.lock = new ReentrantLock();
        this.capacity = capacity;
        this.size = 0;
        this.fullCondition = lock.newCondition();
        this.emptyCondition = lock.newCondition();
    }

    public void produce(int val){
        if(val < 0) {
            return;
        }
        // 获取锁对象
        lock.lock();
        try{
            // 当实际存量大于等于容量时，当前线程进入等待状态
            if(size >= capacity){
                System.out.println("当前库存:"+size+">"+"容量,当前线程停止");
                fullCondition.await();
            }
            int inc = (size +val) > capacity ? capacity - size : val;
            size+=inc;
            System.out.println("库存增加:"+inc+",目前库存为:"+size);
            // 有存入时唤醒休眠中的当前锁被当前条件限制的进程
            emptyCondition.signal();
        }catch (Exception e){
        }finally {
            lock.unlock();
        }
    }
}
