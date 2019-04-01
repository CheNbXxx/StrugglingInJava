package chenbxxx.io.nio;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 结合Selector的NIO服务端
 *
 * @author chen
 * @date 19-4-1
 */
@Slf4j
public class NioSocketServerWithSelector {
    public static void main(String[] args) throws IOException {
        NioSocketServerWithSelector.startServer();
    }

    private static final Integer PORT = 8080;

    private static final String HOST = "localhost";

    private static final Integer BUFFER_SIZE = 1024;

    private static final ByteBuffer READ_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
    private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

    private static final AtomicInteger CLIENT_SIZE = new AtomicInteger();

    static {
        WRITE_BUFFER.put(("已成功连接到服务端:"+CLIENT_SIZE.incrementAndGet()).getBytes());
        WRITE_BUFFER.mark();
    }


    private static void startServer() throws IOException {
        // 打开服务端
        ServerSocketChannel server = ServerSocketChannel.open();

        // 监听某个端口
        server.socket().bind(new InetSocketAddress(HOST, PORT));

        // 设置非阻塞模式
        // !!! channel只有在非阻塞模式下才能和selector绑定
        server.configureBlocking(false);

        // 打开选择器
        Selector selector = Selector.open();

        // 选择感兴趣的事件
        // 一共有以下四种:
        // 1. OP_READ - channel可读
        // 2. OP_WRITE - channel可写
        // 3. OP_ACCEPT - channel有连接
        // 4. OP_CONNECT - channel可连接
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            // 没有准备好的通道
            if(selector.select() <= 0){
                continue;
            }

            // 获取全部准备好的通道集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                // 获取并删除待处理的事件
                SelectionKey next = iterator.next();

                // 删除防止重复处理
                iterator.remove();

                // 有通道待连接
                if (next.isAcceptable()) {
                    // 建立通道并注册到selector
                    server.accept()
                            .configureBlocking(false)
                            .register(selector, SelectionKey.OP_READ);
                    continue;
                }

                // 有通道可读
                if (next.isReadable()) {
                    // 获取内容并展示
                    SocketChannel channel = (SocketChannel) next.channel();
                    channel.read(READ_BUFFER);
                    READ_BUFFER.flip();
                    log.info(new String(READ_BUFFER.array()));
                    READ_BUFFER.clear();
                    continue;
                }

                // 有通道可读
                if (next.isWritable()) {
                    // 获取内容并展示
                    ((SocketChannel) next.channel()).write(WRITE_BUFFER);
                    WRITE_BUFFER.reset();
                    continue;
                }
            }
        }
    }
}
