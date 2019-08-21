package chenbxxx.example.netty.decoder_test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * EmbeddedChannel的测试类
 *
 * @author chen
 * @date 2019/8/13 下午11:02
 */
@Slf4j
public class FrameChunkDecoderTest {

    @Test
    public void testFrameChunkDecoder() {
        ByteBuf byteBuf = Unpooled.copiedBuffer("012345678910".getBytes());
        ByteBuf input = byteBuf.duplicate();

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new FrameChunkDecoder(50), new SysOutHandler());

        int i = 0;
        while (input.readableBytes() >= 4) {
            embeddedChannel.writeInbound(input.readBytes(4));
        }
    }

    /**
     * 规定最大报文长度
     */
    static class FrameChunkDecoder extends ByteToMessageDecoder {

        private Integer allowMaxFrameLength;

        public FrameChunkDecoder(Integer allowMaxFrameLength) {
            this.allowMaxFrameLength = allowMaxFrameLength;
        }

        /**
         * ByteToMessageDecoder 为一类特殊的ChannelHandlerAdapter
         *
         * @param ctx 绑定的处理上线
         * @param in  输入的消息
         * @param out 解码后的消息,应该被加到这里
         * @throws Exception
         */
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            // 获取可读取的字节总数
            int byteCount = in.readableBytes();

            if (byteCount > allowMaxFrameLength) {
                // 释放对应内存,丢弃该帧
                in.release();

                throw new TooLongFrameException("ding ding ding mother fuck");
            }

            out.add(in.readBytes(byteCount));
        }
    }

    /**
     * Message to Integer
     */
    static class MessageToIntegerDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (in.readableBytes() > 4) {
                out.add(in.readInt());
            }
        }
    }

    static class SysOutHandler extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.info("{},receiver msg ", ((ByteBuf) msg).toString(CharsetUtil.UTF_8));
        }
    }
}
