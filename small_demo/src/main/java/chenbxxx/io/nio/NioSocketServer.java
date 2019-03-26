package chenbxxx.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * NIO的ServerSocket例子,服务端
 *
 * @author chen
 * @date 19 -3-26
 */
@Slf4j
public class NioSocketServer {
    /**
     * 端口信息
     */
    private static final int PORT = 8888;

    /**
     * 主机地址
     */
    private static final String HOST = "localhost";

    /**
     * 线程名称后缀
     */
    private static int threadSuffix = 0;

    /**
     *  业务处理线程池
     */
    static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(10, 15, (long) 60.0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                    r -> new Thread(r,"Service Thread " + ++threadSuffix));

    /**
     * 开启NIO服务端
     *
     * @throws IOException 服务建立异常
     */
    private void startServer() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(HOST, PORT));
        // 设置非阻塞模式
        server.configureBlocking(false);

        log.info("Service has started,host is {} and listening on port {}", HOST, PORT);

        while (true) {
            /*
             * 在非阻塞模式下,`accept`方法会立即返回,
             * 在阻塞模式下,会一直阻塞直到有链接请求.
             */
        }
    }
}
