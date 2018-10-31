package top.chenbxxx.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-30
 */
@Configuration
/** 启用缓存 */
@EnableCaching
public class RedisConfig {


    /**
     * 自定义生成key的规则
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (o, method, objects) -> {
            //格式化缓存key字符串
            StringBuilder sb = new StringBuilder();
            //追加类名
            sb.append(o.getClass().getName());
            //追加方法名
            sb.append(method.getName());
            //遍历参数并且追加
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            System.out.println("调用Redis缓存Key : " + sb.toString());
            return sb.toString();
        };
    }

    /**
     * 采用RedisCacheManager作为缓存管理器
     * @param connectionFactory
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }


    /**
     * 配置RedisTemplate配置信息
     * @param connectionFactory 连接工厂
     * @return
     */
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();

        // 更改key和Value序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }
}
