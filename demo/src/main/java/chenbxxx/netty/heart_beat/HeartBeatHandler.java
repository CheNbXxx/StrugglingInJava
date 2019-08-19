package chenbxxx.netty.heart_beat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳的业务逻辑类
 *
 * @author chen
 * @date 2019/8/19 上午8:37
 */
@Slf4j
public class HeartBeatHandler extends ChannelHandlerAdapter {

    /**
     * 心跳包
     */
    private static final ByteBuf HEART_SIGN = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.UTF_8));

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            log.info("send the heart sign");
            // 发送心跳包,失败后关闭
            ctx.writeAndFlush(HEART_SIGN.duplicate())
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            return;
        }

        super.userEventTriggered(ctx,evt);
    }
}
