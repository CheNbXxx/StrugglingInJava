package chenbxxx.design_patterns;

/**
 * @author chen
 * @date 2020/6/13 下午6:19
 */
public class Prototype implements Cloneable{

    private final String a = "111";

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        Prototype prototype = new Prototype();
        for (int i = 0;i < 10;i++){
            System.out.println(prototype.clone());
        }
    }
}

