package top.chenbxxx.rabbitmq.comsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-7
 */
@Slf4j
@Component
@RabbitListener(queues = "test-queue")
public class Comsumer {


    @RabbitHandler
    public void handler(String msg) {
        log.info("=============== get Message ================");
        log.info("message" + msg);
    }
}
