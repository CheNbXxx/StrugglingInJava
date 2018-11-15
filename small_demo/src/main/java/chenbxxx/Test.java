package chenbxxx;

import java.util.concurrent.TimeUnit;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2018/11/8 10:55
 */
public class Test implements Runnable{
    public static void main(String[] args) throws InterruptedException {
        ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
        threadLocal.set(10);
        threadLocal.set(11);
        threadLocal.set(12);
    }

    @Override
    public void run() {

    }
}

