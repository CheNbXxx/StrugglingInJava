package chenbxxx.netty.websocket_im;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * ChannelPipeline对象的初始化工厂类
 * 和EventLoopGroup绑定,每个请求过来之后都会根据initChannel生成对应的管道
 *
 * @author chen
 * @date 2019/8/17 下午11:55
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {

    /**
     * 保存所有流经的管道信息
     */
    private final ChannelGroup channels;

    public ChatServerInitializer(ChannelGroup eventLoopGroup) {
        this.channels = eventLoopGroup;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
               .addLast(new HttpServerCodec())
                .addLast(new ChunkedWriteHandler())
                .addLast(new HttpObjectAggregator(64 * 1024))
                .addLast(new HttpRequestHandler("ws"))
                .addLast(new WebSocketServerProtocolHandler("ws"))
                .addLast(new TextWebSocketFrameHandler(channels));
    }
}
