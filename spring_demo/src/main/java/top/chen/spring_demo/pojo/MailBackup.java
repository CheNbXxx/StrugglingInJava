package top.chen.spring_demo.pojo;

import lombok.*;
import top.chen.spring_demo.enums.MailSendSign;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.util.Date;

/**
 * @author chen
 * @description 邮件备份表实体类
 *      使用JPA相关注解指明映射表，使用hibernate.validator做数据校验
 * @email ai654778@vip.qq.com
 * @date 18-12-20
 */
@Data
@Table(name = "mail_backup")
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MailBackup implements Serializable {
    private static final long serialVersionUID = 7757828479496597796L;
    /** 主键*/
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收方*/
    @Email
    @Column(name = "to_",length = 320)
    private String to_;

    /** 主体内容*/
    @Column(name = "content",length = 1024)
    private String content;

    /** 发送标记 0未发送 1已发送*/
    @Column(name = "send_sign")
    @Enumerated(EnumType.ORDINAL)
    private MailSendSign sendSign;

    /** 发送时间*/
    @Past
    @Column(name = "gmt_send")
    private Date gmtSend;

    /** 创建时间*/
    @Past
    @Column(name = "gmt_create")
    private Date gmtCreate;

    /** 修改时间*/
    @Past
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
