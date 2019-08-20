package chenbxxx.actual.sticky_bag;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 粘包的客户端方法,仅完成指定次数的消息发送
 *
 * @author chen
 * @date 2019/8/19 下午11:42
 */
@Slf4j
public class StickyBagClient {
    public static void start(String host, int port) {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new CycleSendMsgHandler(100, "If we can only encounter each other rather than stay with each other,then I wish we had never encountered."))
                .remoteAddress(host, port)
                .connect()
                .syncUninterruptibly()
                .channel()
                .closeFuture()
                .syncUninterruptibly();

        log.info("client start.........");
    }


    public static void main(String[] args) {
        log.info("length : [{}]", "If we can only encounter each other rather than stay with each other,then I wish we had never encountered.".length());
        StickyBagClient.start("localhost", 8083);

    }
}
