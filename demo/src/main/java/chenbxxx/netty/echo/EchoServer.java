package chenbxxx.netty.echo;

import chenbxxx.netty.decoder_test.DelimitedDecoderTest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Echo服务引导类
 *
 * @author chen
 * @date 2019/8/7 上午7:42
 */
@Slf4j
public class EchoServer {
    /**
     * 端口
     */
    private final int port;

    private Channel channel;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        final EchoServerHandler echoServerHandler = new EchoServerHandler();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(10);
        ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoopGroup,eventLoopGroup)
                    // 制定所使用的NIO传输Channel
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    // 配置长链接
                    .option(ChannelOption.SO_KEEPALIVE,true);
                    // 指定端口设置套接字地址
//                    .localAddress(new InetSocketAddress(port))
                    // 添加一个到子Channel
//                    .childHandler(new DelimitedDecoderTest.LineBasedHandlerInitializer());
            // 异步绑定服务器,调用sync阻塞等待绑定完成
            ChannelFuture f = bootstrap.bind(port).sync();

            f.addListener((ChannelFutureListener) future -> {
                if(f.isSuccess()) {
                    log.info("// ============= server start success");
                }else{
                    future.cause().printStackTrace();;
                }
            });
            // 获取CloseFuture
            f.channel().closeFuture().sync();
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoServer(8080).start();
    }
}
