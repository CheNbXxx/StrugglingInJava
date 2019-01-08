package chenbxxx;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 18-12-27
 */
@Slf4j
public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        int i = 10;

        int j = 101;

        j = i = i+1;

        log.info("j:[{}],i:[{}]",j,i);

        StringBuilder s1= new StringBuilder("asgiudqbwjeuirwqegorurtgyhhhhheqwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwqw");

        for (int a = 0;a < 100000;a++){
            s1.append("12312312312313");
        }
        String s = s1.toString();

        long l = System.currentTimeMillis();
        byte[] bytes = s.getBytes();
        System.out.println(System.currentTimeMillis() - l);

        long l1 = System.currentTimeMillis();
        char[] chars = s.toCharArray();
        System.out.println(System.currentTimeMillis() - l1);

    }

    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (16 - 1));
    }
}
