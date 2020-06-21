package chenbxxx.design_patterns;

/**
 * @author chen
 * @date 2020/6/21 上午11:00
 */
public class BridgeMode {

    interface Memory{

        void store();
    }

    class SamsungMemory implements Memory{

        @Override
        public void store() {
            System.out.println("三星的内存");
        }
    }

    class ToshibaMemory implements Memory{

        @Override
        public void store() {
            System.out.println("东芝的内存");
        }
    }

    static abstract class Computer{
        Memory memory;

        public Computer(Memory memory) {
            this.memory = memory;
        }


        abstract void doSomething();
    }

    class MyComputer extends Computer {

        public MyComputer(Memory memory) {
            super(memory);
        }

        @Override
        public void doSomething() {
            super.memory.store();
        }


    }

}
