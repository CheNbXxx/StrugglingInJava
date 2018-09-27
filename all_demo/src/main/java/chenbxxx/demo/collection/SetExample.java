package chenbxxx.demo.collection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/26
 */
public class SetExample {
    public static void main(String[] args) {
        HashSet<intVal> hashSet = new HashSet<>();

        hashSet.add(new intVal(1));
        hashSet.add(new intVal(3));
        hashSet.add(new intVal(2));

       showSet(hashSet);
    }

    static class intVal{
        int i;

        intVal(int i){
            this.i = i;
        }

        @Override
        public int hashCode() {
            return i;
        }

        @Override
        public boolean equals(Object obj) {
            return true;
        }

        @Override
        public String toString() {
            return i+"";
        }
    }

    static <T> void showSet(Set<T> set){
        Iterator<T> iterator = set.iterator();

        while (iterator.hasNext()){
            System.out.println( iterator.next() );
        }
    }

}
