package chenbxxx.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.locks.AbstractOwnableSynchronizer;

/**
 * Http服务端启动类
 *
 * @author chen
 * @date 2019/8/18 下午7:26
 */
public class HttpServer {

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static final EventLoopGroup subGroup = new NioEventLoopGroup();

    public static void start(Integer port){
        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(bossGroup, subGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpChannelInitializer());

            bootstrap.bind(port)
                    .syncUninterruptibly()
                    .channel()
                    .closeFuture()
                    .syncUninterruptibly();
        }finally {
            bossGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        HttpServer.start(8081);
    }
}

