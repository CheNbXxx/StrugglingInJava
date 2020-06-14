package chenbxxx.design_patterns;

import java.awt.*;

/**
 * @author chen
 * @date 2020/6/14 下午11:41
 */
public class BuilderMode {
    class Director{

        private Builder interBuilder = new InterBuilder();
        private Builder amdBuilder = new AMDBuilder();

        public Computer builder(){
            final Computer computer = new Computer();
            computer.cpu = interBuilder.cpu();
            computer.memory = amdBuilder.memory();
            return computer;
        }

    }

    class Computer{
        private String cpu;

        private String memory;
    }

    interface Builder{
        String cpu();

        String memory();
    }

    class InterBuilder implements Builder{

        @Override
        public String cpu() {
            return "Inter CPU";
        }

        @Override
        public String memory() {
            return "Inter Memory";
        }
    }

    class AMDBuilder implements Builder{

        @Override
        public String cpu() {
            return "AMD CPU";
        }

        @Override
        public String memory() {
            return "AMD Memory";
        }
    }





}
