package chenbxxx.example.netty.decoder_test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于分割符的解码测试
 *
 * @author chenooooooooooooooooooooooooooooooooooo/. * @date 2019/8/14 下午10:21
 */
@Slf4j
public class DelimitedDecoderTest {

    public void mainTest() {
//                EmbeddedChannel embeddedChannel = new EmbeddedChannel(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("*".getBytes())),new DisplayChannelHandler());
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new LineBasedFrameDecoder(64 * 1024), new DisplayChannelHandler());

        embeddedChannel.writeInbound(Unpooled.copiedBuffer("Hello \n World".getBytes()));
    }

    static class LineBasedHandlerInitializer extends ChannelInitializer {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline()
                    .addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("*".getBytes())))
                    .addLast(new DisplayChannelHandler());
        }
    }

    /**
     * 仅仅作展示使用,输出全部的输入
     */
    static class DisplayChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.info("receiver msg :[{}]", ((ByteBuf) msg).toString(CharsetUtil.UTF_8));
        }
    }
}
