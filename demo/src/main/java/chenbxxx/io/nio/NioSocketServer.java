package chenbxxx.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

    public static void main(String[] args) throws IOException, InterruptedException {
        NioSocketServer.startServer();
    }
    /**
     * 端口信息
     */
    private static final int PORT = 8888;

    /**
     * 主机地址
     */
    private static final String HOST = "localhost";

    /**
     * 缓冲区大小
     */
    private static final Integer CACHE_SIZE = 1024;

    /**
     * 线程名称后缀
     */
    private static int threadSuffix = 0;

    /**
     *  业务处理线程池
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(10, 15, (long) 60.0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                    r -> new Thread(r,"Service Thread " + ++threadSuffix));

    /**
     * 开启NIO服务端
     *
     * @throws IOException 服务建立异常
     */
    private static void startServer() throws IOException, InterruptedException {
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
            final SocketChannel accept = server.accept();
            if(accept != null && accept.isOpen()){
                log.info("||====== Socket连接成功....");
                THREAD_POOL_EXECUTOR.execute(new HandleRunnable(accept));
            }

            TimeUnit.SECONDS.sleep(10);
        }
    }

    @Slf4j
    private static class HandleRunnable implements Runnable{

        private SocketChannel accept;
        private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        HandleRunnable(SocketChannel accept){
            this.accept = accept;
        }

        @Override
        public void run() {
            // 从socketChannel中获取信息
            try {
                accept.read(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteBuffer.flip();
            log.info("客户端的信息:{}",byteBuffer.toString());
        }
    }
}
