package top.chen.spring_demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import top.chen.spring_demo.enums.MailSendSign;
import top.chen.spring_demo.pojo.CustomizeProperty;
import top.chen.spring_demo.pojo.MailBackup;
import top.chen.spring_demo.repository.MailBackupRepository;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringDemoApplicationTests {

	@Resource
	private MailBackupRepository backupRepository;
	@Resource
	private CustomizeProperty customizeProperty;

	@Resource
	private RedisTemplate<String,MailBackup> redisTemplate;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Test
	public void contextLoads() {
		MailBackup helloWorld = MailBackup.builder()
				.to_("chenbxxx@gmail.com")
				.content("HelloWorld")
				.sendSign(MailSendSign.NOT_SEND)
				.gmtCreate(new Date())
				.gmtModified(null)
				.gmtSend(null)
				.build();

		backupRepository.saveAndFlush(helloWorld);
	}

	@Test
	public void propertiesTest(){
	    log.info("// ======================== "+ customizeProperty.toString() );
	}

	@Test
	public void redisTest(){
		MailBackup demo = MailBackup.builder()
				.to_("chenbxxx@gmail.com")
				.content("HelloWorld")
				.sendSign(MailSendSign.NOT_SEND)
				.gmtCreate(new Date())
				.gmtModified(null)
				.gmtSend(null)
				.build();

		redisTemplate.opsForValue().set("he1",demo);

		MailBackup mailBackup = redisTemplate.opsForValue().get("he1");
		log.info("// ==================MailBackup:[{}]",mailBackup);

	}
}
