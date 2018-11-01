package top.chenbxxx.demo.DAO;

import top.chenbxxx.demo.entity.Product;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
public interface ProductDao {

    /**
     * key值前缀常量
     */
    String PERFIX = "product_";

    /**
     * 添加Product到Redis服务器
     * @param product
     * @throws Exception
     */
    boolean insert(Product product) throws Exception;

    /**
     * 根据id查找Product
     * @param id
     * @return
     * @throws Exception
     */
    Product getById(Integer id) throws Exception;

    /**
     * 更新
     * @param product
     * @throws Exception
     */
    boolean update(Product product) throws Exception;

    /**
     * 删除
     * @param id
     * @throws Exception
     */
    boolean delete(Integer id) throws Exception;
}
