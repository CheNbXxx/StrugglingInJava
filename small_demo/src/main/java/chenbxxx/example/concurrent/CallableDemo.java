package chenbxxx.example.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-2
 */
@Slf4j
public class CallableDemo implements Callable<String> {

    private static final Double RANDOM_NUMBER = 0.55;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println(0 << 10);
        FutureTask<String> stringFuture = new FutureTask<String>(new CallableDemo());
        stringFuture.run();
        log.info("获得运行结果:[{}]",stringFuture.get());
    }

    @Override
    public String call() throws Exception {
        log.info("// ====== 进入call()");
        if(Math.random() > RANDOM_NUMBER) {
            return "Success";
        }else{
            return "Fail";
        }
    }
}
