package top.chenbxxx.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.chenbxxx.demo.entity.Product;
import top.chenbxxx.demo.entity.User;
import top.chenbxxx.demo.mapper.ProductMapper;
import top.chenbxxx.demo.mapper.UserMapper;
import top.chenbxxx.demo.service.ProductService;
import top.chenbxxx.demo.utils.RedisUtil;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootMybatisApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductService productService;

    @Resource
    private RedisUtil redisUtil;

    @Test
    public void testProduct(){
        System.out.println("******** start *********");
        Product product = productMapper.selectById(1);
        System.out.println(product);
    }

    @Test
    public void userSelectByIdTest() {
        System.out.println("************* start ***************");
        User user = userMapper.selectById(1);
        System.out.println(user);
    }

    @Test
    public void ProductServiceTest(){
        Product product = productService.getById(1);
        System.out.println(product);
    }

    @Test
    public void RedisTest(){
        redisUtil.set("testKey3","0");
        System.out.println("END");
    }

}
