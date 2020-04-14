package top.chenbxxx;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

/**
 * @author chen
 * @date 2020/4/8 下午10:04
 */
@Slf4j
public class JunitTest {

    @Test()
    @Tag("Hello")
    public void test(){
        log.info("HelloWorld");
        Assert.assertFalse(false);
    }
}
