package top.chenbxxx.demo.mapper;

import org.apache.ibatis.annotations.*;
import top.chenbxxx.demo.entity.User;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-23
 */
@Mapper
public interface UserMapper {


    @Select("SELECT * FROM t_user WHERE id=#{id}")
    @Results(value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "userAccount", column = "user_account"),
            @Result(property = "userPasswd", column = "user_passwd"),
            @Result(property = "userSalt",column = "user_salt")
    })
    User selectById(@Param("id") Integer id);

    @Delete("DELETE FROM t_user WHERE id = #{uid}")
    int deleteById(@Param("uid") int uid);
}
