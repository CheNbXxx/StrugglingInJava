package chenbxxx.demo.collection;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/27
 */
public class HashMapExample {
    public static void main(String[] args) {
//        System.out.println(HashMapExample.tableSizeFor(4));
        System.out.println(3 ^ 5);

        System.out.println(Integer.toBinaryString(-8));
        System.out.println(Integer.toBinaryString(-8 >>> 1));

        System.out.println(Integer.toBinaryString(8));
        System.out.println(Integer.toBinaryString(8 >> 1));
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        System.out.println(n);
        n |= n >>> 2;
        System.out.println(n);
        n |= n >>> 4;
        System.out.println(n);
        n |= n >>> 8;
        System.out.println(n);
        n |= n >>> 16;
        System.out.println(n);
        return n+1;
    }
    public void showName(){
        System.out.println(this.getClass().getSimpleName());
    }
}