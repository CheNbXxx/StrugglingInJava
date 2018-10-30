package top.chenbxxx.demo.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-24
 */
@ToString
@Data
public class Product implements Serializable {
    private Integer id;

    private String productName;

    private Integer productNum;

    private Date productSmtCreate;

    private static final long serialVersionUID = 1L;
}
