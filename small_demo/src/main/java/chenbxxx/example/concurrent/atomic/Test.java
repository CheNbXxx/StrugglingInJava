package chenbxxx.example.concurrent.atomic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-1
 */
@Slf4j
public class Test{
    public static void main(String[] args) {
        TestClass testClass = new TestClass(10);
        AtomicIntegerFieldUpdater<TestClass> atomicIntegerFieldUpdater = AtomicIntegerFieldUpdater.newUpdater(TestClass.class,"testField");
        int i = atomicIntegerFieldUpdater.get(testClass);
        log.info("// ===== i:[{}]",i);
        atomicIntegerFieldUpdater.addAndGet(testClass,10);
        i = atomicIntegerFieldUpdater.get(testClass);
        log.info("// ===== i:[{}]",i);
        atomicIntegerFieldUpdater.accumulateAndGet(testClass,10,(n,m) -> n+m);
        i = atomicIntegerFieldUpdater.get(testClass);
        log.info("// ===== i:[{}]",i);
    }

    @Data
    @AllArgsConstructor
    private static class TestClass{
        /**
         * 使用原子属性更新类的要求:
         * 1. 字段必须是volatile修饰的,保证线程间的可见性.
         * 2. 必须是实例变量,而非静态变量,不能用static修饰.
         * 3. 必须是可修改的,不能用final修饰.
         */
        volatile int testField;


    }
}
