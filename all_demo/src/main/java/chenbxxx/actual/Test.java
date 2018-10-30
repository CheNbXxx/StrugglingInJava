package chenbxxx.actual;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-27
 */
public class Test {
    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(10,10,
                        10,  TimeUnit.MINUTES,new LinkedBlockingQueue<>(),Executors.defaultThreadFactory());

        threadPoolExecutor.execute(() -> {
            try {
                new CopyFileByMultithread(3).copyFile(new File("/home/chen/test"),new File("/home/chen/testMain3"));
            } catch (IOException e) {
                e.printStackTrace();
            };
        });

    }
}
