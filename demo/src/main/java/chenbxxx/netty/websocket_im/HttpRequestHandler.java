package chenbxxx.netty.websocket_im;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 处理http请求
 *
 * @author chen
 * @date 2019/8/17 下午11:10
 */
@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * WebSocket的路径规则
     */
    private final String webSocketUrl;

    /**
     * 首页地址
     */
    private static File indexPage;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            final String path = location.toURI() + "index.html";
            indexPage = new File( !path.contains("file:") ? path : path.substring(5));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public HttpRequestHandler(String wsUri){
        this.webSocketUrl = wsUri;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        log.info("uri:[{}]",uri);
        if(uri.equalsIgnoreCase(webSocketUrl)){
            // 跳过 让后面的Handler处理
            // 触发其后的ChannelHandler事件
            ctx.fireChannelRead(request);
            return;
        }
        log.info("not a websocket request");
//        RandomAccessFile randomAccessFile = new RandomAccessFile("index.html", "r");
        HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
        // 设置请求体大小
//        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,String.valueOf(randomAccessFile.length()));

        // 设置Keep Alive
        if(HttpHeaderUtil.isKeepAlive(request)){
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        }

        // 将index.html写到客户端
        ctx.write("HelloWorld");


        ChannelFuture lastContentFuture = ctx
                .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //如果不支持keep-Alive，服务器端主动关闭请求
        lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }
}
