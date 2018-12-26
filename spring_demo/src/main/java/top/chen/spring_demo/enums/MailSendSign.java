package top.chen.spring_demo.enums;

import lombok.AllArgsConstructor;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 18-12-21
 */
@AllArgsConstructor
public enum MailSendSign {

    /** 已发送*/
    NOT_SEND(1,"已发送"),

    /** 未发送*/
    SEND(0,"未发送")
    ;
    private Integer key;
    private String value;


    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
