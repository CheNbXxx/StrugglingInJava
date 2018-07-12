package chenbxxx;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/10
 */
public class Main {
    public static void main(String[] args) {
//        new BlockingQueueExample();
        AtomicInteger atomicInteger = new AtomicInteger(10);
        System.out.println(atomicInteger.get());
        System.out.println(atomicInteger.getAndAdd(10));
        System.out.println(atomicInteger.get());
        System.out.println(atomicInteger.addAndGet(10));
        System.out.println(atomicInteger.get());
        System.out.println(atomicInteger.accumulateAndGet(10,(a,b)-> a+b));
        System.out.println(atomicInteger.get());
    }
}
