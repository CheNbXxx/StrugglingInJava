package chenbxxx.demo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/31
 */
public class demo {
    public static void main(String[] args) {
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(1L);
        list.add(5L);
        list.add(2L);
        list.add(5L);
        list.add(4L);

        System.out.println(list.stream().distinct().collect(Collectors.toList()).toString());
    }
}
