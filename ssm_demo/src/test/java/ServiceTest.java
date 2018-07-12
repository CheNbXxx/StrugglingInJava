import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/6
 */
@RunWith(SpringJUnit4ClassRunner.class)
//配置文件的位置
//若当前配置文件名=当前测试类名-context.xml 就可以在当前目录中查找@ContextConfiguration()
@ContextConfiguration("classpath*:/springConfigs/*.xml")
public class ServiceTest {
    @Resource
//    private UserService userService;

    @Test
    public void UserTest(){
    }
}
