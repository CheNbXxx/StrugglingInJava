import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/24
 */
@Slf4j
public class WeiXinUtil {
    private static final String APP_ID = "wxa67b0a13a2cb21a1";

    private static final String APP_SECRET = "40512c798f4a8e4476fdbe76423bc7ce";

    private static final String SCHEME = "https";

    private static final String HOST = "api.weixin.qq.com";

    /**
     * 获取`access_token`,需要`appid`,`grant_type`,`secret`,`grant_type`固定为`client_credential`
     * 完整URI：https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
     * method：get
     */
    private static final String PATH_ACCESS_TOKEN = "cgi-bin/token";

    /**
     * 获取TICKET需要`access_token`,`type`两个参数,`type`固定为`jsapi`
     * 完整URI：https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi
     * method: get
     */
    private static final String PATH_TICKET = "cgi-bin/ticket/getticket";

    private static final String ACCESS_TOKEN = "13_NY45zG9V06hUm7O2ZTnaKqQ8ekOxiPEJCY5xCNiQk4PP1uiMHP6adcfYwelZjFPdUIMaKYo2SCK64Y_p-jdmuRc5a3ecxlze7M8blJCSgcRMJpJ-gHhAAb_DtyIPBMiAEAHTE";


    private String getAccessToken(String appId,String secret) throws URISyntaxException {
        String result = "";

        // 构建uri
        URI weiXinUri = new URIBuilder()
                .setScheme(SCHEME)
                .setHost(HOST)
                .setPath("cgi-bin/token")
                .setParameter("grant_type","client_credential")
                .setParameter("appid",appId)
                .setParameter("secret",secret)
                .build();
        log.info("uri:{}",weiXinUri);

        // 发送请求
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(weiXinUri);
            try(CloseableHttpResponse closeableHttpResponse = client.execute(httpget)) {
                StatusLine statusLine = closeableHttpResponse.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
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

                    // 获取`access_token`
                    result = JSONObject.fromObject(stringBuilder.toString()).getString("access_token");
                    log.info("AccessToken: {}",result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void main(String[] args) throws URISyntaxException {
        new WeiXinUtil().getAccessToken(APP_ID,APP_SECRET);
    }
}
