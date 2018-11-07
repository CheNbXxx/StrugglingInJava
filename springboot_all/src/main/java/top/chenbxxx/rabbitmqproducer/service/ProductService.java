package top.chenbxxx.rabbitmqproducer.service;

import top.chenbxxx.rabbitmqproducer.entity.Product;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-6
 */
public interface ProductService {

    /**
     * 主键查找
     * @param id  主键
     * @return  商品对象
     * @throws Exception 预防性
     */
    Product getById(Long id) throws Exception;
}
