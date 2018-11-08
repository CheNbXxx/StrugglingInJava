package chenbxxx.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * function:
 *
 * @author CheNbXxx
 * @email chenbxxx@gmail.con
 * @date 2018/11/1 12:42
 */
public class Receiving {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("118.24.134.237");
        factory.setUsername("chen");
        factory.setPassword("19951217");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
        ) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + message + "'");
                }
            };
            channel.basicConsume(QUEUE_NAME, true, consumer);
        }
    }
}
