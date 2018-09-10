package chen.example;

import chen.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBoot1Application.class)
@Slf4j
public class ServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void contextLoads() {
        log.info(userService.findById(1).toString());
    }
}
