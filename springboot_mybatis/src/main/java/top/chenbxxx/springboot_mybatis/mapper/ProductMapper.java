package top.chenbxxx.springboot_mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.chenbxxx.springboot_mybatis.entity.Product;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-24
 */
@Mapper
public interface ProductMapper {

    Product selectById(@Param("id") Integer pid);
}
