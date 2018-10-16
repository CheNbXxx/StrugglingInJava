package chenbxxx.demo.concurrent.imitate;

import lombok.Data;

/**
 * `LRU`算法是在最近最少使用算法,可以作为一种缓存淘汰算法。

1. 定长的底层数据结构
2. 将get()的数据放到底层数据结构的开头或者结尾
3. 若长度不够,删除结尾或开头
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/10/16
 */
public class LRUDemo {

    @Data
    private class Node{
        private int key;
        private String value;
        private Node next;
    }
}
