package chenbxxx.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author bingxin.chen
 * @date 2019/4/2 09:34
 */
@Slf4j
public class NioSocketClientWithSelector {

    private static final String HOST = "127.0.0.1";

    private static final Integer PORT = 8080;

    private static final Integer BUFFER_SIZE = 1024;
    private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);
    private static final ByteBuffer READ_BUFFER = ByteBuffer.allocate(BUFFER_SIZE);

    static {
        WRITE_BUFFER.mark();
        WRITE_BUFFER.put("HELLO WORLD".getBytes());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. 打开Socket通道,并绑定到指定的IP和端口
        SocketChannel open = SocketChannel.open();
        Selector selector = Selector.open();
        if(!open.connect(new InetSocketAddress(HOST,PORT))) {
            log.info("||====== 连接服务端失败");
            return;
        }
        open.configureBlocking(false);
        open.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        log.info("||====== 连接服务端成功");
        while (true){
            TimeUnit.SECONDS.sleep(3);
            if(selector.select() <= 0) {
                log.info("||====== 无可用通道");
                continue;
            }
            final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()){
                SelectionKey next = iterator.next();
                iterator.remove();

                if(next.isWritable()){
                    ((SocketChannel)next.channel()).write(WRITE_BUFFER);
                    WRITE_BUFFER.reset();
                }
                if(next.isReadable()){
                    SocketChannel channel = (SocketChannel) next.channel();
                    channel.read(READ_BUFFER);
                    READ_BUFFER.flip();
                    log.info("服务端发送来的消息:{}",new String(READ_BUFFER.array()));
                    READ_BUFFER.clear();
                }
            }
        }
    }
}
