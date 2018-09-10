package chen.example;

import chen.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringBoot1ApplicationTests {

    @Resource
    private UserService userService;

    @Test
    public void contextLoads() {
        log.info(userService.findById(1).toString());
    }

}
