package top.chen.spring_demo.config;

import net.sf.json.JSONObject;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import top.chen.spring_demo.pojo.MailBackup;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author chen
 * @description
 *      自定义序列化方式,继承RedisSerializer接口
 *    此处仅仅时简单的将MailBackup转化为Json字符串
 * @email ai654778@vip.qq.com
 * @date 18-12-23
 */
public class MailBackupRedisSerializer implements RedisSerializer<MailBackup> {
    @Override
    public byte[] serialize(MailBackup mailBackup) throws SerializationException {
        if (mailBackup == null) {
            return new byte[]{};
        }
        return JSONObject.fromObject(mailBackup).toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public MailBackup deserialize(byte[] bytes) throws SerializationException {
        if (Objects.isNull(bytes) || bytes.length <= 0) {
            return null;
        }
        return (MailBackup) JSONObject
                .toBean(JSONObject.fromObject(new String(bytes,StandardCharsets.UTF_8)),
                        MailBackup.class);
    }
}
