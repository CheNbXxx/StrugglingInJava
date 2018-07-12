package chenbxxx;

import java.util.concurrent.BlockingQueue;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/10
 */
public class Consumer implements Runnable{
    protected BlockingQueue<Integer> blockingQueue;

    Consumer(BlockingQueue<Integer> blockingQueue){
        this.blockingQueue = blockingQueue;
    }

    public void run() {
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
    }
}
