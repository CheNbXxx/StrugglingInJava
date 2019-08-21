package chenbxxx.example.netty.sticky_bag;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Tcp粘包现象的展示类
 * (仅仅作展示)
 * <p>
 * Netty实现的仅仅是应用层的逻辑,往下实际上走的还是TCP/IP协议
 * 按照OSI七层模型来说,TCP协议属于传输层
 * TCP协议中存在MSS(最大分段长度)的说法,类似于IP层的MTU(最大传输单元)
 * 因此我们需要传输的数据会在传输层被截短或者附加.
 *
 * @author chen
 * @date 2019/8/19 下午11:29
 */
@Slf4j
public class StickyBagServer {

    public static void start(int port) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new StickyBagChannelInitializer())
                .bind(port)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();

        log.info("server is starting......");
    }

    public static void main(String[] args) {
        StickyBagServer.start(8083);
    }
}
