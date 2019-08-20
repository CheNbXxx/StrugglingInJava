package chenbxxx.actual.netty_protocol_buffer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chen
 * @date 2019/8/20 下午11:35
 */
@Slf4j
public class ProtocolClient {
    public static void connection(String host, int port) {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ProtoBufClientChannelInit())
                .connect(host, port)
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();
    }


    public static void main(String[] args) {
        ProtocolClient.connection("localhost", 8088);
    }
}
