package top.chenbxxx.demo.DAO.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import top.chenbxxx.demo.DAO.ProductDao;
import top.chenbxxx.demo.entity.Product;

import java.util.Objects;

/**
 * 以String格式在Redis操作对象
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
@Repository
@Slf4j
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public boolean insert(Product product) throws Exception {
        // 如果id为空 指定id
        if(Objects.isNull(product.getId())){
            product.setId(Objects.requireNonNull(redisTemplate.keys(PERFIX + "*")).size()+1);
        }
        redisTemplate.opsForValue().set(PERFIX+product.getId(),product);
        return true;
    }

    @Override
    public Product getById(Integer id) throws Exception {
        return (Product) redisTemplate.opsForValue().get(PERFIX+id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean update(Product product) throws Exception {
        if(Objects.isNull(product.getId())){
            return false;
        }
        if(Objects.isNull(redisTemplate.opsForValue().get(PERFIX+product.getId()))){
            log.info("id:{}的Product对象不存在,更新失败");
            return false;
        }
        redisTemplate.opsForValue().set(PERFIX+product.getId(),product);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean delete(Integer id) throws Exception {
        if(Objects.isNull(id)){
            return false;
        }
        Boolean flag = redisTemplate.hasKey(id);
        if(Objects.isNull(flag) || !flag){
            return false;
        }

        return redisTemplate.delete(id);
    }
}
