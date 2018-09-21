package chenbxxx.demo.collection;

import java.util.Vector;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/20
 *
 *  记录Vector一些不常见的操作
 */
public class VectorExample {
    public static void main(String[] args) {
        Vector<Integer> vector = new Vector<>(1);

        // 允许空值
        vector.add(null);
    }
}
