package chenbxxx.demo.concurrent.imitate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Set;

/**
 * `LRU`算法是在最近最少使用算法,可以作为一种缓存淘汰算法。

1. 定长的底层数据结构
2. 将get()的数据放到底层数据结构的开头或者结尾
3. 若长度不够,删除结尾或开头
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/10/16
 */
public class LRUDemo implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * 计算Hash的方法直接照搬`HashMap`
     * @param key
     * @return
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    private class Node{
        private int key;
        private String value;
        private Node next;
    }
}
