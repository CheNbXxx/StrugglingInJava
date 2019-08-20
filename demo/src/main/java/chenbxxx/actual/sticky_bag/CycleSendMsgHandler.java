package chenbxxx.actual.sticky_bag;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chen
 * @date 2019/8/19 下午11:46
 */
@Slf4j
public class CycleSendMsgHandler extends ChannelHandlerAdapter {

    /**
     * 循环发送的次数
     */
    private final int MAX_CYCLE;

    /**
     * 发送的消息主体
     */
    private final String CONTENT;

    public CycleSendMsgHandler(int maxCycle, String content) {
        this.MAX_CYCLE = maxCycle;
        this.CONTENT = content;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf byteBuf = Unpooled.copiedBuffer(CONTENT, CharsetUtil.UTF_8);
        for (int i = 0; i < MAX_CYCLE; ) {
            ctx.writeAndFlush(byteBuf.retain());
            log.info("send success time :[{}]", ++i);
            // 粘包的现象发生在高并发的情况下
            // 延迟一秒之后服务端处理时间绰绰有余 就没有粘包现象了
//            TimeUnit.SECONDS.sleep(1);
        }

        byteBuf.release();
    }


}
