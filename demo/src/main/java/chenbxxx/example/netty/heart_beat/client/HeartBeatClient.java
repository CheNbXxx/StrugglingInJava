package chenbxxx.example.netty.heart_beat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 心跳检测的客户端
 *
 * @author bingxin.chen
 * @date 2019/8/19 12:45
 */
public class HeartBeatClient {

    private static final EventLoopGroup subWorker = new NioEventLoopGroup(1);

    public static void connection(String host, int port) {
        Bootstrap bootStrap = new Bootstrap();
        bootStrap.group(subWorker)
                .channel(NioSocketChannel.class)
                .handler(new DisplayHandler());

        bootStrap.connect(host, port)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();
    }

    public static void disConnection() {
        subWorker.shutdownGracefully();
    }


    public static void main(String[] args) {
        HeartBeatClient.connection("localhost", 8082);

        Runtime.getRuntime().addShutdownHook(new Thread(HeartBeatClient::disConnection));
    }
}
