package bio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chen
 * @description BIO即为同步阻塞IO
 * @email ai654778@vip.qq.com
 * @date 19-1-30
 */
@Slf4j
public class Server {
    /** 端口号信息 */
    private static final int PORT = 8888;

    /** 线程池*/
    private static final ThreadPoolExecutor threadPool;

    static {
        threadPool = new ThreadPoolExecutor(6,8,10, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>(),new SimpleThreadFactory());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Server().start();
    }

    private void start() throws IOException, InterruptedException {
        log.info("|| ======== 服务已启动，端口号为[{}]",PORT);
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            while (true) {
                final Socket accept = serverSocket.accept();
                log.info("|| ======== 接受到一个请求,移交线程池处理");
                threadPool.execute(new HandleRunnable(accept));

                TimeUnit.SECONDS.sleep(5);
            }
        }
    }

    /**
     * 请求的处理线程
     */
    static class HandleRunnable implements Runnable{
        private Socket socket;

        private AtomicInteger atomicInteger = new AtomicInteger(0);

        HandleRunnable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            log.info("|| ======== 线程[{}]开启",Thread.currentThread().getName());
            // 请求处理逻辑
            try (OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream()){
                // 包装输入输出流 字节流变为字符流
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8));
                String message = null;
                while (Objects.nonNull(message = bufferedReader.readLine())){
                    log.info("|| ======== 接收到信息：[{}]",message);
                    printWriter.println("确认接受到第["+atomicInteger.incrementAndGet()+"]条信息");
                    printWriter.flush();
                    message = null;
                    TimeUnit.SECONDS.sleep(5);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class SimpleThreadFactory implements ThreadFactory{

        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"serverThread"+atomicInteger.incrementAndGet());
        }
    }

}
