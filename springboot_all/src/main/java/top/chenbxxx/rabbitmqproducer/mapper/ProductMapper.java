package top.chenbxxx.rabbitmqproducer.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.chenbxxx.rabbitmqproducer.entity.Product;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-6
 */
@Mapper
public interface ProductMapper {

    /**
     * 主键查找
     * @param id    主键
     * @return  没有返回空
     * @throws Exception 预防类跑出错
     */
    Product getById(Long id) throws Exception;
}
