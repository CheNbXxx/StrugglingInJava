package chenbxxx.design_patterns;

import java.io.*;

/**
 * 工厂方法模式
 *
 * @author chen
 * @date 2020/6/14 下午9:30
 */
public class FactoryMethod {
    class BufferReaderFactory implements chenbxxx.design_patterns.FactoryMethodInterface {

        @Override
        public BufferedReader create(File file) throws FileNotFoundException {
            final FileInputStream in = new FileInputStream(file);
            final InputStreamReader in1 = new InputStreamReader(in);
            return new BufferedReader(in1);
        }
    }

    class FileReaderFactory implements chenbxxx.design_patterns.FactoryMethodInterface {

        @Override
        public Reader create(File file) throws FileNotFoundException {
            return new FileReader(file);
        }
    }

    interface FactoryMethodInterface {
        Reader create(File file) throws FileNotFoundException;
    }
}



