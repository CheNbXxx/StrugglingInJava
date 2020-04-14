package top.chenbxxx;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chen
 * @date 2020/4/8 下午10:16
 */
@Slf4j
@RunWith(Parameterized.class)
public class RunWithParameterized {

    @Parameterized.Parameters
    public static List getParameters() {
        return Arrays.asList(new Object[][]{
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}
        });
    }

    /**
     * Parameter的value表示参数下标,默认为0
     */
    @Parameterized.Parameter(1)
    public int i;

    @Parameterized.Parameter(0)
    public int j;


    public RunWithParameterized() {
    }

    private static AtomicInteger COUNT = new AtomicInteger(0);

    @Test
    public void test() {
        log.info("i:[{}],j:[{}]", i, j);
        log.info("第{}次测试", COUNT.incrementAndGet());
    }
}
