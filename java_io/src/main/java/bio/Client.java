package bio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author chen
 * @description BIO Socket的客户端
 * @email ai654778@vip.qq.com
 * @date 19-1-30
 */
@Slf4j
public class Client {

    private static final int PORT = 8888;

    private static final String IP = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        new Client().sendRequest();
    }

    private void sendRequest() throws IOException {
        // 创建时就会发送请求
//        Socket socket = new Socket(IP,PORT);
        Socket socket = new Socket();
        // 链接并设置超时时间
        socket.connect(new InetSocketAddress(IP,PORT),1000);
        // 保活
        socket.setKeepAlive(true);

        try (OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()){
            // 包装输入输出流 字节流变为字符流
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8));

            String ackMessage = null;

            while (true){
                printWriter.println("Hello,I'm Jack Ma");
                printWriter.flush();
                while (true) {
                    if (Objects.nonNull(ackMessage = bufferedReader.readLine())) {
                        log.info("|| ======== 接受确认信息[{}]", ackMessage);
                        break;
                    }
                }
            }
        }
    }
}
