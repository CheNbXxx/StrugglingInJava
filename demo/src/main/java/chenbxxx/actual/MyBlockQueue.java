package chenbxxx.actual;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chen
 * @date 2020/5/8 下午9:24
 */
public class MyBlockQueue<E> extends LinkedList<E> implements BlockingQueue<E>{

    private volatile int size;

    private Object lock;

    public MyBlockQueue(int size){
        this.size = size;
        lock = new Object();
    }

    @Override
    public E poll() {
        return super.poll();
    }

    @Override
    public boolean offer(E e) {
        return super.offer(e);
    }

    @Override
    public void put(E e) throws InterruptedException {

    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public E take() throws InterruptedException {
        return null;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
}
