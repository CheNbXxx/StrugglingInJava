package chenbxxx.example.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 业务逻辑
 *
 * @author chen
 * @date 2019/8/18 下午8:30
 */
@Slf4j
public class HttpServerChannelHandler extends ChannelHandlerAdapter {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            throw new RuntimeException("msg class is error");
        }

        try {
            // 打印uri
            FullHttpRequest request = (FullHttpRequest) msg;
            log.info("receiver request to :[{}]", request.uri());

            // 打印Http的版本
            HttpVersion httpVersion = request.protocolVersion();
            log.info("protocol version :[{}]", httpVersion.text());

            // 打印Http请求的方法
            final HttpMethod method = request.method();
            log.info("method name : [{}]", method.name());

            // 打印请求头
            HttpHeaders httpHeaders = request.headers();
            List<Map.Entry<CharSequence, CharSequence>> entries = httpHeaders.entries();
            log.info("header info :");
            for (Map.Entry<CharSequence, CharSequence> entry : entries) {
                log.info("head key:[{}], value :[{}]", entry.getKey(), entry.getValue());
            }

            // 输出
            final HttpResponse httpResponse = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.OK, Unpooled.copiedBuffer("Ding Ding Ding,Mother Fuck".getBytes()));
            ChannelFuture channelFuture = ctx.channel().writeAndFlush(httpResponse);
            channelFuture.addListener(ChannelFutureListener.CLOSE);

        } finally {
            ((FullHttpRequest) msg).release();
        }
    }
}
