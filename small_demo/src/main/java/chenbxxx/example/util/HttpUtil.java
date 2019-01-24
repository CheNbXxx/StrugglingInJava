package chenbxxx.example.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@Slf4j
public class HttpUtil {
    /**
     * 获取Http请求的所有响应信息
     * Get 方式
     *
     * @param uri 目标URI
     * @return
     */
    public static JSONObject getHttpGetResponse(URI uri) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(uri);
            try (CloseableHttpResponse closeableHttpResponse = client.execute(httpget)) {
                StatusLine statusLine = closeableHttpResponse.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    // 获取Http实体
                    HttpEntity entity = closeableHttpResponse.getEntity();
                    // 获取输入流并读取全部的信息
                    InputStream inputStream = entity.getContent();
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] buf = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buf)) != -1) {
                        stringBuilder.append(new String(buf, 0, length, StandardCharsets.UTF_8));
                    }
                    inputStream.close();
                    // 将读取到的信息转化为JSON
                    log.info("StringBuilder Info:{}", stringBuilder);
                    return JSONObject.fromObject(stringBuilder.toString());
                }

                log.info("URI:{} 的远程连接出错,错误码为:{}", uri, statusLine.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("URI:{} 的远程连接出错", uri);
        }
        return new JSONObject();
    }
}
