package top.chenbxxx.demo.service;

import top.chenbxxx.demo.entity.Product;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
public interface ProductService {

    /**
     * 根据id查找单个对象
     * @param id
     * @return
     */
    Product getById(Integer id);

    /**
     * 添加对象
     * @param product
     * @return
     */
    boolean insert(Product product);

    /**
     * 根据id删除对象
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 更新对象
     * @param product
     */
    void updateById(Product product);
}
