package chenbxxx.demo.concurrent.example;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/19
 */
public class ReentrantLockExample {
    public static void main(String[] args) {
        // 默认为非公平锁
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
    }
}
