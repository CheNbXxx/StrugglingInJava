package top.chenbxxx.rabbitmq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-11-6
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Alias("product")
public class Product implements Serializable {

    private static final long serialVersionUID = -1163692328742455456L;
    /** 主键 */
    private Long id;

    /** 商品名称 */
    private String productName;

    /** 商品编号 */
    private String productNum;

    /** 商品创建时间 */
    private Date productSmtCreate;
}
