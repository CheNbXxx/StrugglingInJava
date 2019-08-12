package chenbxxx.netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    // 指定端口设置套接字地址
//                    .localAddress(new InetSocketAddress(port))
                    // 添加一个到子Channel
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
//                                    .addLast(new IdleStateHandler(20,20,10))
                                    .addLast("handler2",new SimpleChannelInboundHandler(){
                                        @Override
                                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                            log.info("// ============== add channel handler");
                                            super.handlerAdded(ctx);
                                        }
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            ByteBuf byteBuf = (ByteBuf)msg;
                                            log.info("11111" + byteBuf.toString(StandardCharsets.UTF_8));
                                            ctx.fireChannelRead(msg);
                                        }
                                        @Override
                                        protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            ByteBuf byteBuf = (ByteBuf)msg;
                                            log.info(byteBuf.toString(StandardCharsets.UTF_8));
                                        }
                                    })
                                    .addLast("handler1",echoServerHandler)
                            ;
                        }
                    });
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
