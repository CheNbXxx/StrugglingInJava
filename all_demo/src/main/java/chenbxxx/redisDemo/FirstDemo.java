package chenbxxx.redisDemo;

import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-23
 */
public class FirstDemo {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("118.24.134.237");
        jedis.auth("19951217");

        Set<String> keys = jedis.keys("*");


        System.out.println("***************** static *****************");
        for (String key :keys){
            System.out.print("key："+key);
            System.out.print(" value:"+jedis.get(key));
            System.out.println();
        }
    }
}
