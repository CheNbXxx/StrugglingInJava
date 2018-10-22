package chenbxxx.demo.collection.example;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/27
 */
public class HashExample {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(new HashExample().hashCode() % 12);
    }

    protected void show(){
        System.out.println("protected test");
    }
}

