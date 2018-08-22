import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/14
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        try (CloseableHttpClient client = HttpClients.createDefault();){
            URI uri = new URIBuilder()
                        .setScheme("http")
                        .setHost("localhost")
                        .setPath("garden/field/fieldDetail")
                        .setPort(8888)
                        .setParameter("id","8")
                        .build();

            log.info("URI:{}",uri);
            HttpGet httpget = new HttpGet(uri);

            try(CloseableHttpResponse closeableHttpResponse = client.execute(httpget);) {
                // 请求状态
                StatusLine statusLine = closeableHttpResponse.getStatusLine();

                // 请求头
                Header[] allHeaders = closeableHttpResponse.getAllHeaders();
                for (Header header : allHeaders){
                    log.info("HeaderName:{},HeaderValue:{}",header.getName(),header.getValue());
                }

                HttpEntity entity = closeableHttpResponse.getEntity();

                log.info("content:{},ContentEncoding:{},ContentLength:{},ContentType:{}",entity.getContent(),entity.getContentEncoding(),entity.getContentLength(),entity.getContentType());

                InputStream inputStream = entity.getContent();

                StringBuilder stringBuilder = new StringBuilder();
                byte[] buf=new byte[1024];
                int length;
                if(( length = inputStream.read(buf)) != -1){
                    log.info(new String(buf));
                    stringBuilder.append(new String(buf,0,length));
                }

//                String result = new String(chars, StandardCharsets.UTF_8);

//                log.info("string result:{}",result);

                JSONObject jsonObject = JSONObject.fromObject(stringBuilder.toString());

                log.info("json result:{}",jsonObject);


                log.info(Arrays.asList(1,2,3,4).toString());
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
