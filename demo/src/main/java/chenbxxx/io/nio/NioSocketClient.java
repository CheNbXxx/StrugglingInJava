package chenbxxx.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO Socket的客户端
 *
 * @author chen
 * @date 19-3-31
 */
public class NioSocketClient {

    private static final String HOST = "127.0.0.1";

    private static final Integer PORT = 8080;

    public static void main(String[] args) throws IOException {
        try(
                // 1. 打开Socket通道,并绑定到指定的IP和端口
              SocketChannel open = SocketChannel.open()
                ) {
            open.connect(new InetSocketAddress(HOST,PORT));
            // 2. 发消息
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put("HelloWorld".getBytes());
            byteBuffer.flip();
            open.write(byteBuffer);
        }
    }
}
