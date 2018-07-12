package chenbxxx;

import java.util.concurrent.BlockingQueue;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/10
 */
public class Producer implements Runnable {
    protected BlockingQueue<Integer> blockingQueue;
    Producer(BlockingQueue<Integer> blockingQueue){
        this.blockingQueue = blockingQueue;
    }
    public void run() {
        try {
            blockingQueue.put(1);
            Thread.sleep(500);
            blockingQueue.put(2);
            Thread.sleep(500);
            blockingQueue.put(3);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
