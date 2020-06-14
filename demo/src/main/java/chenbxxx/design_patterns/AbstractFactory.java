package chenbxxx.design_patterns;

import java.io.*;

/**
 * @author chen
 * @date 2020/6/14 下午10:01
 */
public class AbstractFactory {
    class BufferReaderFactory implements AbstractFactoryInterface {

        @Override
        public BufferedReader createReader(File file) throws FileNotFoundException {
            final FileInputStream in = new FileInputStream(file);
            final InputStreamReader in1 = new InputStreamReader(in);
            return new BufferedReader(in1);
        }

        @Override
        public Writer createWriter(File file) throws FileNotFoundException {
            final FileOutputStream out = new FileOutputStream(file);
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            return new BufferedWriter(outputStreamWriter);
        }
    }

    class FileReaderFactory implements AbstractFactoryInterface {

        @Override
        public Reader createReader(File file) throws FileNotFoundException {
            return new FileReader(file);
        }

        @Override
        public Writer createWriter(File file) throws IOException {
            return new FileWriter(file);
        }
    }

    interface AbstractFactoryInterface {
        Reader createReader(File file) throws FileNotFoundException;

        Writer createWriter(File file) throws IOException;
    }
}



