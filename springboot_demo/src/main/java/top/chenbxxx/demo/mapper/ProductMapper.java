package top.chenbxxx.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.chenbxxx.demo.entity.Product;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-24
 */
@Mapper
public interface ProductMapper {

    Product selectById(@Param("id") Integer pid);
}
