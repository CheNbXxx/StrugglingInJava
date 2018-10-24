package top.chenbxxx.springboot_mybatis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.chenbxxx.springboot_mybatis.entity.Product;
import top.chenbxxx.springboot_mybatis.entity.User;
import top.chenbxxx.springboot_mybatis.mapper.ProductMapper;
import top.chenbxxx.springboot_mybatis.mapper.UserMapper;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootMybatisApplicationTests {

    @Resource
    private UserMapper mapper;

    @Resource
    private ProductMapper productMapper;

    @Test
    public void testProduct(){
        System.out.println("******** start *********");
        Product product = productMapper.selectById(1);
        System.out.println(product);
    }

    @Test
    public void contextLoads() {
        System.out.println("************* start ***************");
        System.out.println(mapper);
        User user = null;
        try {
            user = mapper.selectById(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(user);
    }

}
