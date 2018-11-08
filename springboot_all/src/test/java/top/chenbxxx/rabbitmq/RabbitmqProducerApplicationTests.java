package top.chenbxxx.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.chenbxxx.rabbitmq.entity.Product;
import top.chenbxxx.rabbitmq.service.ProductService;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
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
    public void rabbitTemplateTest() throws InterruptedException {

        // 不论RoutingKey是否正确,Exchange对了就会输出true
        // ==> ConfirmCallback是检测是否发送到Exchange
        rabbitTemplate.setConfirmCallback(
                (correlationData, b, s) ->
                        log.info("Confirm ==> correlationData:"+correlationData+",ACK:"+b+",cause:"+s));

        rabbitTemplate.setReturnCallback(
                (message, i, s, s1, s2) ->
                        log.info("Return ==> Message:"+message+",replyCode:"+i+"replyText:"+s+",exchange:"+s1+",routingKey:"+s2));

        rabbitTemplate.convertAndSend("chenbxxx1","HelloWorld");
        TimeUnit.SECONDS.sleep(10000);
    }

}
