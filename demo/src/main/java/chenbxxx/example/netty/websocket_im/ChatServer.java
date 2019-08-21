package chenbxxx.example.netty.websocket_im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 基于Netty.WebSocket的聊天室
 *
 * @author chen
 * @date 2019/8/17 下午11:08
 */
@Slf4j
public class ChatServer {

    /**
     * 保存登录的Channel
     */
    private static final ChannelGroup channels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    public static void start(InetSocketAddress address) {
        new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChatServerInitializer(channels))
                .bind(address)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();
    }

    /**
     * 释放系统资源
     */
    public static void destroy() {
        channels.close();
        eventLoopGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        ChatServer.start(new InetSocketAddress(9999));

        Runtime.getRuntime().addShutdownHook(new Thread(ChatServer::destroy));
    }
}
