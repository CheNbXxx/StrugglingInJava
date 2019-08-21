package chenbxxx.example.netty.heart_beat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 心跳检测的管道初始化类
 *
 * @author chen
 * @date 2019/8/19 上午8:44
 */
public class HeartBeatInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel ch) {
        ch.pipeline()
                .addLast(new IdleStateHandler(0, 0, 3, TimeUnit.SECONDS))
                .addLast(new HeartBeatHandler());
    }
}
