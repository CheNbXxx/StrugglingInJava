package chenbxxx;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author CheNbXxx
 * @email ai654778@vip.qq.com
 * @date 2018/7/11
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // 加载配置文件并获得sqlSessionFactory
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        // 开启回话 并获取相应会话
        SqlSession session = sqlSessionFactory.openSession();
    }
}
