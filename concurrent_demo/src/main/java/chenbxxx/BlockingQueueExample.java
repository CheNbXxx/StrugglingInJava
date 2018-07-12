package chenbxxx;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/10
 */
public class BlockingQueueExample {
    public BlockingQueueExample(){
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<Integer>(10);
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
    }
}
