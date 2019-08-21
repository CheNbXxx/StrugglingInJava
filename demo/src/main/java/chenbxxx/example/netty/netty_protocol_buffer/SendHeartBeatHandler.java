package chenbxxx.example.netty.netty_protocol_buffer;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chen
 * @date 2019/8/20 下午11:38
 */
@Slf4j
public class SendHeartBeatHandler extends ChannelHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        HeartBeatProto.HeartBeat.Builder builder = HeartBeatProto.HeartBeat.newBuilder();
        HeartBeatProto.HeartBeat heartBeat = builder.setHeartId(1)
                .setHeartTime("1995-12-17")
                .build();
        log.info("send message");
        ctx.writeAndFlush(heartBeat);
    }
}
