package chenbxxx.example.netty.heart_beat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 心跳检测服务
 *
 * @author chen
 * @date 2019/8/18 下午11:39
 */
public class HeartBeatServer {

    private static final EventLoopGroup bossWorker = new NioEventLoopGroup(1);

    private static final EventLoopGroup subWorker = new NioEventLoopGroup();

    public static void start(int port) {
        new ServerBootstrap().group(bossWorker, subWorker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HeartBeatInitializer())
                .bind(port)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();
    }

    public static void close() {
        bossWorker.shutdownGracefully();
        subWorker.shutdownGracefully();
    }

    public static void main(String[] args) {
        HeartBeatServer.start(8082);
    }
}
