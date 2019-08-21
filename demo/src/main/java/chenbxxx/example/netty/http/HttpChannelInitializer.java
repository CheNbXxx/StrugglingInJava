package chenbxxx.example.netty.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Http的管道初始化类
 *
 * @author chen
 * @date 2019/8/18 下午8:29
 */
public class HttpChannelInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(64 * 1024))
                .addLast(new HttpServerChannelHandler());
    }
}
