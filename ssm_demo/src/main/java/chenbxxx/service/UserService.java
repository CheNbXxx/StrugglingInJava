package chenbxxx.service;

import chenbxxx.dto.User;
import chenbxxx.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/11
 */
@Service("userService")
public class UserService {

    @Resource
    private UserMapper mapper;

    public void insert(User user){
        mapper.insert(user);
    }

//    public
}
