package chenbxxx.demo;

import java.awt.geom.Area;
import java.util.*;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/26
 */
public class Messy {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        Integer[] integerss = new Integer[list.size()];

        list.toArray(integerss);

        System.out.println("输出integerss");
        show(Arrays.asList(integerss));

        Arrays.fill(integerss,2);

        System.out.println("输出list");
        List<Integer> integers = Arrays.asList(integerss);
        show(Arrays.asList(integers));

        System.out.println("end");
    }


    private static void show(Collection collection){
        Iterator iterator = collection.iterator();

        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
