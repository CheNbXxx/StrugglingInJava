package chenbxxx.http;

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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

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

    /** 16进制转化字符 */
    private static final char[] HEXES = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    /**
     * 获取`access_token`,需要`appid`,`grant_type`,`secret`,`grant_type`固定为`client_credential`
     * 完整URI：https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
     * method：get
     */
    private static final String PATH_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

    /**
     * 获取TICKET需要`access_token`,`type`两个参数,`type`固定为`jsapi`
     * 完整URI：https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi
     * method: get
     */
    private static final String PATH_TICKET = "cgi-bin/ticket/getticket";

    private static final String ACCESS_TOKEN = "13_qY7U1TyLoOInd0kVojbgvcOU4TgNnFlUfuphx2I7VQcZB22qBOCyoeMgKt8yYq95NMzWNkm-okQ_4O8gERYsuDMFLyWGVRjQafn4-6trXmtlB-3_uVF2HgRnFCVVL8ILo9ka7rHpFTFVbkFXTNRcAAAMGK";
    private static final String TICKET = "HoagFKDcsGMVCIY2vOjf9giOEJw8ZFtv7Vz_NlZQFCTZ_RhJsqKB-DaReU099rmDaDZhXp4wyW9xYBDNaRluxw";


    /**
     * 调用微信接口获取`Access_Token`
     * @param appId     公众号唯一标识
     * @param secret    秘钥
     * @return
     *      {"access_token":"ACCESS_TOKEN","expires_in":7200}
     * @throws URISyntaxException
     */
    private String getAccessToken(String appId,String secret) throws URISyntaxException {
        String result = "";

        // 构建uri
        URI weiXinUri = new URIBuilder()
                .setScheme(SCHEME)
                .setHost(HOST)
                .setPath(PATH_ACCESS_TOKEN)
                .setParameter("grant_type","client_credential")
                .setParameter("appid",appId)
                .setParameter("secret",secret)
                .build();

        URI uri =new URI(PATH_ACCESS_TOKEN);

        log.info("uri:{}",weiXinUri);

        // 发送请求
        // 1.创建可关闭客户端`client`
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            // 2.执行的是`GET`请求
            HttpGet httpget = new HttpGet(weiXinUri);
            // 3.获取Http响应
            try(CloseableHttpResponse closeableHttpResponse = client.execute(httpget)) {
                // 4.解析响应
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
                    JSONObject resultJson = JSONObject.fromObject(stringBuilder.toString());

                    // 获取`access_token`
                    result = resultJson.getString("access_token");
                    log.info("AccessToken: {}",result);
                    if(Objects.isNull(result)){
                        // 获取错误信息
                        int errcode = resultJson.getInt("errcode");
                        String errmsg = resultJson.getString("errmsg");
                        log.info("微信接口调用错误,errcode:{},errmsg:{}",errcode,errmsg);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据`access_token`获取`ticket`
     * @param accessToken   access_token
     * @return
     */
    private String getTicket(String accessToken) throws URISyntaxException {
        String result = "";

        // 构建URI
        URI uri = new URIBuilder()
                   .setScheme(SCHEME)
                   .setHost(HOST)
                   .setPath(PATH_TICKET)
                   .setParameter("access_token",ACCESS_TOKEN)
                   .setParameter("type","jsapi")
                   .build();

        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse httpResponse = client.execute(httpGet)){
                HttpEntity entity = httpResponse.getEntity();

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
                JSONObject resultJson = JSONObject.fromObject(stringBuilder.toString());

                // 获取`ticket`
                result = resultJson.getString("ticket");
                log.info("Ticket: {}",result);
                if(Objects.isNull(result)){
                    // 获取错误信息
                    int errcode = resultJson.getInt("errcode");
                    String errmsg = resultJson.getString("errmsg");
                    log.info("微信接口调用错误,errcode:{},errmsg:{}",errcode,errmsg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取JS-JDK权限签名
     * @param url       必须为调用JS接口页面的完整URL
     * @param nonceStr  随机字符串
     * @param timestamp 时间戳
     * @return
     */
    public String getSignature(String nonceStr,String timestamp,String url) throws Exception {

        /**
         * 签名算法流程
         * 1. 签名包含字段有.noncestr（随机字符串）, 有效的jsapi_ticket, timestamp（时间戳）, url（当前网页的URL，不包含#及其后面部分）
         * 2. 对所有待签名参数按照字段名的ASCII 码从小到大排序（字典序）后，使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串string1
         * 3. 对string1进行sha1签名，得到signature
         */
        // 拼接成列表
        List<String> paramsList = new ArrayList<>();
        paramsList.add("jsapi_ticket="+TICKET);
        paramsList.add("noncestr="+nonceStr);
        paramsList.add("timestamp="+timestamp);
        paramsList.add("url="+url);

        // 将所有代签名参数按照`key`的ASCll码从小到达排序
        paramsList.sort(Comparator.comparingInt(p -> p.charAt(0)));

        // list -> string
        String params = String.join("&",paramsList);

        String sha1Result = encrypt(params.getBytes(StandardCharsets.UTF_8),"SHA-1");
        log.info("sha1:{}",sha1Result);

        return sha1Result;
    }

    /**
     * 根据指定的算法加密任意长度的数据, 返回固定长度的十六进制小写哈希值
     *
     * @param data      需要加密的数据
     * @param algorithm 加密算法, 例如: MD5, SHA-1, SHA-256, SHA-512 等
     */
    public static String encrypt(byte[] data, String algorithm) throws Exception {
        // 1. 根据算法名称获实现了算法的加密实例
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        // 2. 加密数据, 计算数据的哈希值
        byte[] cipher = digest.digest(data);

        // 3. 将结果转换为十六进制小写
        return bytes2Hex(cipher);
    }

    /**
     * 根据指定的算法加密文件数据, 返回固定长度的十六进制小写哈希值
     *
     * @param file      需要加密的文件
     * @param algorithm 加密算法, 例如: MD5, SHA-1, SHA-256, SHA-512 等
     */
    public static String encrypt(File file, String algorithm) throws Exception {
        try (InputStream in = new FileInputStream(file);){
            // 1. 根据算法名称获实现了算法的加密实例
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buf = new byte[1024];
            int len = -1;
            while ((len = in.read(buf)) != -1) {
                // 2. 文件数据通常比较大, 使用 update() 方法逐步添加
                digest.update(buf, 0, len);
            }
            // 3. 计算数据的哈希值, 添加完数据后 digest() 方法只能被调用一次
            byte[] cipher = digest.digest();
            // 4. 将结果转换为十六进制小写
            return bytes2Hex(cipher);
        }
    }



    /**
     * byte -> hex
     *   字节->16进制
     * */
    private static String bytes2Hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(HEXES[(b >> 4) & 0x0F]);
            sb.append(HEXES[b & 0x0F]);
        }
        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        // 获取即时
        long currentTimeMillis = System.currentTimeMillis();
        // 获得当前时间戳
        String timestamp  = String.valueOf(currentTimeMillis / 1000);
        // 获取生成签名的字符串
        String nonceStr = UUID.randomUUID().toString().trim().replaceAll("-", "");

        String signature = new WeiXinUtil().getSignature(nonceStr, timestamp, "www.baidu.com");

        log.info("signature:{}",signature);
    }


}
