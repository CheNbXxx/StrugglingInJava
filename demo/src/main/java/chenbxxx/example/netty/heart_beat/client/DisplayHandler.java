package chenbxxx.example.netty.heart_beat.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 展示下心跳包,
 *
 * @author bingxin.chen
 * @date 2019/8/19 12:51
 */
@Slf4j
public class DisplayHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        log.info("heart beat msg:[{}]", byteBuf.toString(CharsetUtil.UTF_8));
        byteBuf.release();
    }
}
