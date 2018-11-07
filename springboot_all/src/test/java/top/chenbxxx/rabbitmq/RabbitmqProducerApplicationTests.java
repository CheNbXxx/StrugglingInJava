package top.chenbxxx.rabbitmq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.chenbxxx.rabbitmq.service.ProductService;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqProducerApplicationTests {

    @Resource
    private ProductService productService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    public void contextLoads() throws Exception {
        System.out.println(productService.getById(1L));
    }

    @Test
    public void rabbitTemplateTest(){
        rabbitTemplate.convertAndSend("chenbxxx","Hello1");
    }

}
