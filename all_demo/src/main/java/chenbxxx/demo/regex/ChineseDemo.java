package chenbxxx.demo.regex;

import java.util.regex.Pattern;

/**
 * Java使用的是Unicode编码,不论中英文都是`两个字节`存储
 * 中文的编码范围在\u4E00 ~ \u9FA5
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-20
 */
public class ChineseDemo {
    public static Pattern pattern = Pattern.compile("[\u4E00-\u9FA5]");
    public static void main(String[] args) {
        System.out.println(pattern.matcher("中").matches());
    }
}
