package top.chenbxxx.springboot_redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import top.chenbxxx.springboot_redis.entity.Product;
import top.chenbxxx.springboot_redis.entity.User;
import top.chenbxxx.springboot_redis.service.ProductService;
import top.chenbxxx.springboot_redis.service.UserService;

import javax.annotation.Resource;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-23
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Resource(name = "userService")
    private UserService userService;

    @Resource
    private ProductService productService;

    @Test
    public void selectByIdTest() {
        User user = null;
        try {
            user = userService.selectById(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("*************** 以下为测试输出 *******************");
        System.out.println(user);
    }

    @Test
    public void productSelectByIdTest(){
        Product product = productService.selectById(1);
        System.out.println(product);
    }
}
