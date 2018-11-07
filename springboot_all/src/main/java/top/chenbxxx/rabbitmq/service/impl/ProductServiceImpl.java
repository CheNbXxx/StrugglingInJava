package top.chenbxxx.rabbitmq.service.impl;

import org.springframework.stereotype.Service;
import top.chenbxxx.rabbitmq.entity.Product;
import top.chenbxxx.rabbitmq.mapper.ProductMapper;
import top.chenbxxx.rabbitmq.service.ProductService;

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
