package top.chenbxxx.demo.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-23
 */
@Data
@ToString
public class User implements Serializable {
     private Integer id;

     private String userAccount;

     private String userPasswd;

     private String userSalt;

     private Date userSmtCreate;

     private Date userSmtModified;

     private static final long serialVersionUID = 1L;

}
