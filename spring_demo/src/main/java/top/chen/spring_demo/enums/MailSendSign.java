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
    NOT_SEND(0,"已发送"),

    /** 未发送*/
    SEND(1,"未发送")
    ;
    private int key;
    private String value;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
