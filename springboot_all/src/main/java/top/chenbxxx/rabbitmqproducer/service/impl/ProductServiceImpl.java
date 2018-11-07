package top.chenbxxx.rabbitmqproducer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.chenbxxx.rabbitmqproducer.entity.Product;
import top.chenbxxx.rabbitmqproducer.mapper.ProductMapper;
import top.chenbxxx.rabbitmqproducer.service.ProductService;

import javax.annotation.Resource;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-6
 */
@Service("productService")
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper mapper;

    @Override
    public Product getById(Long id) throws Exception {
        return mapper.getById(id);
    }
}
