package top.chenbxxx.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.chenbxxx.demo.DAO.ProductDao;
import top.chenbxxx.demo.entity.Product;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Resource
    private ProductDao productDao;

    @Test
    public void contextLoads() throws Exception {
        Product product = new Product();
        product.setId(2);
        product.setProductName("测试商品111");
        product.setProductNum(101);
        product.setProductSmtCreate(new Date());

        System.out.println(productDao.insert(product));
    }


    @Test
    public void getByIdTest() throws Exception {
        Product byId = productDao.getById(2);
        System.out.println(byId);
    }

}
