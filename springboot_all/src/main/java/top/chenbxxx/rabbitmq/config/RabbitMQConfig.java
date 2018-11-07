package top.chenbxxx.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2018/11/7 9:21
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    /**
     * 如果队列或者交换机不存在还会帮忙创建,
     * 单单在配置文件里面`spring.rabbitmq.template.exchange`并不会帮忙创建
     * @return 队列，交换机
     */
    @Bean
    public Queue defaultQueue(){
        return new Queue("test-queue",true,false,false);
    }
    @Bean
    public DirectExchange defaultExchange(){
        return new DirectExchange("test.exchange",true,false);
    }

    /**
     * 绑定交换机和队列
     * @return banding关系
     */
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(defaultQueue()).to(defaultExchange()).with("chenbxxx");
    }

    /**
     * config类中没有声明`RabbitTemplate`时，Spring会使用配置文件中的属性自动初始化，
     * 如果有声明则需要自己创建ConnectionFactory等类初始化RabbitTemplate。
     */

}
