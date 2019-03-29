package chenbxxx.actual;

import java.util.*;

/**
 * 以LinkedHashMap为基底的LRU缓存工具类
 * 访问的数据会被移动到头结点
 *
 * @author bingxin.chen
 * @date 2019 /3/27 11:55
 */
public class LRUForLinkedHashMap extends LinkedHashMap<Object, Object> {

    /**
     * LRU缓存的容量,超过该容量添加时会删除旧数据
     */
    private int capacity;

    /**
     * 简单构造函数,复杂点可以指定HashMap的容量和负载因子
     *
     * @param capacity the capacity
     */
    public LRUForLinkedHashMap(int capacity) {
        // 第三个参数表示是否按照访问顺序排列
        super(16, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * 在该方法中判断什么时候需要删除旧元素
     *
     * @param eldest 传入的是首节点
     * @return true -> 删除;false -> 保留
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
        return size() > capacity;
    }


}
