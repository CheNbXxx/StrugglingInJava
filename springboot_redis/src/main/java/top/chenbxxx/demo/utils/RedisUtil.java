package top.chenbxxx.demo.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    //====================  common ====================

    /**
     * 制定key的失效时间
     * @param key   键
     * @param time  失效时长,单位为s
     * @return
     */
    public boolean expire(String key,long time){
        if(time <= 0){
            return false;
        }
        Boolean expire = redisTemplate.expire(key, time, TimeUnit.SECONDS);
        return Objects.nonNull(expire)
                ? expire
                : false;
    }

    /**
     * 获取key的失效时间
     */

    /**
     * 判断Key是否存在
     */

    /**
     * 删除key
     */

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

    /**
     * 带失效时间的新增
     */

    /**
     * 原子递增
     */

    /**
     * 院子递减
     */
}
