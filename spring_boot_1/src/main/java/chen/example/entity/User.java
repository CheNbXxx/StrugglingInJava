package chen.example.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


@Data
@ToString
@Table(name = "t_user",schema = "InnoDB")
public class User implements Serializable {
    private Integer id;

    private String userAccount;

    private String userPasswd;

    private String userSalt;

    private Date userSmtCreate;

    private Date userSmtModified;

    private static final long serialVersionUID = 1L;


}