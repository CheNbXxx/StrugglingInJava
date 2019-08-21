package chenbxxx.example.netty.netty_protocol_buffer;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chen
 * @date 2019/8/20 下午11:31
 */
@Slf4j
public class DisplayFieldHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 直接将消息转化为想要的类型
        HeartBeatProto.HeartBeat heartBeat = (HeartBeatProto.HeartBeat) msg;
        log.info("receiver heart beat data:heart id :[{}],heart time :[{}]", heartBeat.getHeartId(), heartBeat.getHeartTime());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
