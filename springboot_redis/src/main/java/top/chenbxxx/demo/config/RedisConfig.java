package top.chenbxxx.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
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

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-30
 */
@Configuration
/** 启用缓存 */
@EnableCaching
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 指定缓存的键名生成方式
     *      最后的生成形式为："productCache::top.chenbxxx.demo.service.impl.ProductServiceImpl::getById:1"
     *  也可以在@Cacheable中制定key的格式
     * @return  键
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            // 键名字符串
            StringBuilder sb = new StringBuilder();
            // 增加类的全路径名
            sb.append(target.getClass().getName());
            // 增加方法名
            sb.append("::").append(method.getName()).append(":");
            // 为缓存的每个类制定
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
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
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }

    public static void main(String[] args) {
        System.out.println(Long.valueOf("false"));
    }
}
