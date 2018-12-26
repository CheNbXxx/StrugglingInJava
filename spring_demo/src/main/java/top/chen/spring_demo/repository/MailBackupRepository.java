package top.chen.spring_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import top.chen.spring_demo.pojo.MailBackup;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 18-12-22
 */
public interface MailBackupRepository extends JpaRepository<MailBackup,Long> {
}
