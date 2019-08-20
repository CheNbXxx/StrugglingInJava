package chenbxxx.actual.sticky_bag;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty内置的拆包器有以下集中
 *
 * @author chen
 * @date 2019/8/20 上午8:00
 * @see io.netty.handler.codec.FixedLengthFrameDecoder  指定长度的拆包器
 * @see LengthFieldBasedFrameDecoder  包头附加长度的拆包器
 * @see io.netty.handler.codec.DelimiterBasedFrameDecoder  间隔符拆包器
 * @see io.netty.handler.codec.LineBasedFrameDecoder  按照换行符的拆包器
 */
@Slf4j
public class StickyBagChannelInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                .addLast(new FixedLengthFrameDecoder(106))
//                  .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4))
//                    .addLast(new DelimiterBasedFrameDecoder(64 * 1024, Unpooled.copiedBuffer(".", CharsetUtil.UTF_8)))
                .addLast(new DisplayHandler());
    }
}
