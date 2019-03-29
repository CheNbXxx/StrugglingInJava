package chenbxxx.example.designpattern;

/**
 * function:
 *
 * @author CheNbXxx
 * @email chenbxxx@gmail.con
 * @date 2018/10/31 14:08
 */
public class Unknow {
    public static void main(String[] args) {
        new doClass().do111();
    }
}

interface BaseClass {
    void show();
}

class Hello implements BaseClass {

    @Override
    public void show() {
        System.out.println("HelloWorld");
    }
}

abstract class BaseDo {
    abstract BaseClass get();

    public void do111() {
        get().show();
    }
}

class doClass extends BaseDo {

    @Override
    BaseClass get() {
        return new Hello();
    }
}

