package chenbxxx.example.netty.websocket_im;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * WebSocket的消息处理Handler
 * <p>
 * WebSocket的六种帧
 *
 * @author chen
 * @date 2019/8/17 下午11:19
 * @see io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame  二进制数据
 * @see io.netty.handler.codec.http.websocketx.TextWebSocketFrame   文本数据
 * @see io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame   包含属于上两个数据的帧
 * @see io.netty.handler.codec.http.websocketx.CloseWebSocketFrame   表示一个Close请求
 * @see io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
 * @see io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 保留group,用来群发消息
     */
    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    /**
     * 自定义事件触发逻辑
     *
     * @param ctx 处理器上下文
     * @param evt 处理的事件
     * @throws Exception unknown
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果事件是Http的握手成功事件
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // 将管道中的httpRequestHandler删除
            ctx.pipeline().remove(HttpRequestHandler.class);
            // 将消息推送到group中的所有用户
            // 消息包装成TextWebSocketFrame
            group.writeAndFlush(new TextWebSocketFrame(ctx.channel() + " join the chat room"));
            // 将当前的管道添加到组里面
            group.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 接收到正常的TextWebSocketFrame请求之后,直接推送到各个客户端
        group.writeAndFlush(msg.retain());
    }
}
