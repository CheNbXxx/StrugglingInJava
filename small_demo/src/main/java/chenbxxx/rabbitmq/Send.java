package chenbxxx.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * function:
 *
 * @author CheNbXxx
 * @email chenbxxx@gmail.con
 * @date 2018/11/1 12:33
 */
public class Send {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("118.24.134.237");
        factory.setUsername("chen");
        factory.setPassword("19951217");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "HelloWorld";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
