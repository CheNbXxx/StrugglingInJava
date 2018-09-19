package chenbxxx.demo.collection.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/19
 */
public class ArrayListExample {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();

        // 新增
        list.add(1);


        int[] i = new int[]{1,2,3,4,5,6};
        int[] ints = Arrays.copyOf(i, 10);
        System.arraycopy(i,1,i,2,
                3);
        for (int is : ints){
            System.out.println(is);
        }

        System.out.println("++++++++++++++++++++++++++");
        List<Integer> collect = Arrays.stream(ints).boxed().collect(Collectors.toList());
        for (int is : collect.subList(1,3)){
            System.out.println(is);
        }
    }
}



