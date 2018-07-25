package chenbxxx.example;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/23
 */
public class YieldExample {
    int i = 0;
    public static void main(String[] args) {
        YieldExample yieldExample = new YieldExample();
        new Thread(new MyRunnable(yieldExample)).start();
        new Thread(new MyRunnable(yieldExample)).start();
    }

    public synchronized void show(){
        System.out.println(Thread.currentThread().getName() + " is running,i="+ ++i);
    }
}

class MyRunnable implements Runnable{

    private YieldExample yieldExample;


    public MyRunnable(YieldExample yieldExample) {
        this.yieldExample = yieldExample;
    }

    @Override
    public void run() {
        for (int i =0; i< 10;i++) {
            if(i == 5) {
                Thread.yield();
            }
            yieldExample.show();
        }
    }
}
