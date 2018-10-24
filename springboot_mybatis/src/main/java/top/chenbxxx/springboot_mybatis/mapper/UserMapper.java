package top.chenbxxx.springboot_mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import top.chenbxxx.springboot_mybatis.entity.User;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-23
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM t_user where id = ${id}")
    User selectById(@Param("id") Integer id) throws Exception;
}
