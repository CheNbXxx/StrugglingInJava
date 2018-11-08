package top.chenbxxx.rabbitmq;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import java.io.IOException;


/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-7
 */
@Component
@RabbitListener(queues = "test.queue")
public class Xiaofeizhe {

    @RabbitHandler
    public void handler(Message message, Channel channel) throws IOException {
        System.out.println("============= 接收到消息");
        System.out.println("|=========== ms:"+message);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
