package chenbxxx.example.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Echo的客户端
 *
 * @author chen
 * @date 2019/8/7 上午8:00
 */
@Slf4j
@ChannelHandler.Sharable
public class EchoClientHandle extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello * World!!", CharsetUtil.UTF_8));
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("client receiver : [{}]", msg.toString(CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello World!!", CharsetUtil.UTF_8));
    }


//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
//        log.info("client receiver : [{}]", byteBuf.toString());
//    }
}
