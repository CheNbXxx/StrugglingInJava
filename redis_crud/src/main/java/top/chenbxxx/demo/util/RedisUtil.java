package top.chenbxxx.demo.util;

import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
public class RedisUtil {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //====================  String ====================

    /**
     * String类型获取
     * @param key   键
     * @return      值
     */
    public Object get(String key){
        return key != null
                ? redisTemplate.opsForValue().get(key)
                : null;
    }

    /**
     * String类型新增
     * @param key     键
     * @param value   值q
     * @return  成功=>true 失败=>false
     */
    public boolean set(String key,Object value){
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
