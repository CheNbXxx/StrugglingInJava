package chenbxxx.mycache;

import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-14
 */
public class LRUExtendsLinkedList<E> extends AbstractList<E> {

    private Object[] objects;

    /** MAX_CACHE_SIZE的默认大小 */
    private static final int DEFAULT_CACHE_SIZE = 16;

    /** 该缓存的最大存储数量 */
    private int  cacheSize;

    public LRUExtendsLinkedList(int n){
        super();
        this.cacheSize = n;
    }

    /**
     * 检查底层数据是否大于MAX_CACHE_SIZE个
     * @return  数据大于MAX_CACHE_SIZE个返回true
     */
    private boolean checkcacheSize(){
        return size() < cacheSize;
    }

    @Override
    public E get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
