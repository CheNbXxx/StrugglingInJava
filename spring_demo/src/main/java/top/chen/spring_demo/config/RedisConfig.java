package top.chen.spring_demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import top.chen.spring_demo.pojo.MailBackup;

/**
 * @author chen
 * @description
 *      redis相关配置类
 * @email ai654778@vip.qq.com
 * @date 18-12-23
 */
@Slf4j
@Configuration
@EnableCaching
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisConfig {


    /**
     * 配置redisTemplate 并自定义序列化方式在
     * @param lettuceConnectionFactory  连接工厂
     * @return RedisTemplate
     */
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, MailBackup> redisCacheTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, MailBackup> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        // 使用自定义的格式转换
        template.setValueSerializer(new MailBackupRedisSerializer());
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(lettuceConnectionFactory);
        log.info("// ================== redis自定义序列化完成");
        return template;
    }
}
