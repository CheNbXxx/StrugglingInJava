package top.chenbxxx.demo.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-31
 */
@ToString
@Data
public class Product implements Serializable {
    private Integer id;

    private String productName;

    private Integer productNum;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date productSmtCreate;

    private static final long serialVersionUID = 1L;
}
