package top.chenbxxx.demo.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.chenbxxx.demo.entity.Product;
import top.chenbxxx.demo.mapper.ProductMapper;
import top.chenbxxx.demo.service.ProductService;

import javax.annotation.Resource;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
@Service("productService")
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper mapper;

    @Cacheable(value = "productCache",key = "'product_'+#id")
    @Override
    public Product getById(Integer id) {
        return mapper.selectById(id);
    }

    @Override
    public boolean insert(Product product) {
        return false;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void updateById(Product product) {

    }
}
