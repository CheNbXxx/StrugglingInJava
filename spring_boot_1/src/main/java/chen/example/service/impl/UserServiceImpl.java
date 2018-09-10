package chen.example.service.impl;

import chen.example.mapper.UserMapper;
import chen.example.entity.User;
import chen.example.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/10
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    @Override
    public User findById(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }
}
