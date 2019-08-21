package chenbxxx.example.netty.netty_protocol_buffer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * ProtocolBuffer的Netty测试服务端类
 * <p>
 * ProtocolBuffer是谷歌
 *
 * @author chen
 * @date 2019/8/20 下午10:49
 */
@Slf4j
public class ProtocolServer {

    private static final EventLoopGroup bossWorker = new NioEventLoopGroup(1);
    private static final EventLoopGroup workWorker = new NioEventLoopGroup();

    public static void start(int port) {
        new ServerBootstrap()
                .group(bossWorker, workWorker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ProtoBufChannelInit())
                .bind(port)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();

    }

    public static void main(String[] args) {
        ProtocolServer.start(8088);
    }
}
