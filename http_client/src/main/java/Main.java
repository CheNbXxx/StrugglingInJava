import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/14
 */
public class Main {
    public static void main(String[] args) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet("https://www.baidu.com");
            System.out.println("Executing request " + httpget.getRequestLine());

            CloseableHttpResponse closeableHttpResponse = client.execute(httpget);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
