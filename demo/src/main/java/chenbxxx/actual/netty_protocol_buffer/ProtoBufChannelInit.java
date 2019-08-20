package chenbxxx.actual.netty_protocol_buffer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chen
 * @date 2019/8/20 下午11:28
 */
@Slf4j
public class ProtoBufChannelInit extends ChannelInitializer {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(HeartBeatProto.HeartBeat.getDefaultInstance()))
                .addLast(new DisplayFieldHandler());

    }
}
