package chenbxxx.jdk;

import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author CheNbXxx
 * @description     Java引用关系的测试类。
 * @email chenbxxx@gmail.con
 * @date 2018/11/8 11:46
 */
@Slf4j
public class ReferenceTest {

    static ArrayList arrayList = new ArrayList();

    public static void main(String[] args) throws InterruptedException {
        WeakReference<Object> weakReference = new WeakReference<>(new Object());

        log.info(weakReference.get()+"");

        System.gc();

        log.info(weakReference.get()+"");
    }
}
