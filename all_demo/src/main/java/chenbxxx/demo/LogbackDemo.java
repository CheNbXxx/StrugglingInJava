package chenbxxx.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/8
 */
@Slf4j
public class LogbackDemo {
    public static void main(String[] args) {
        List<Integer> ints = Arrays.asList(1,0,1,0,1,0,1,1,1);

        ints.sort((i1,i2) -> i1 - i2);

        for (Integer i : ints){
            System.out.println(i);
        }
    }

}
