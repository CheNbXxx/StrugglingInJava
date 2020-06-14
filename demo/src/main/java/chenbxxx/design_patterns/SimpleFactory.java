package chenbxxx.design_patterns;

import java.io.*;

/**
 * 简单工厂模式
 *
 * 产品类就是Reader
 *
 * @author chen
 * @date 2020/6/13 下午7:44
 */
public class SimpleFactory {
    public Reader getRead(File file) throws FileNotFoundException {
        final FileInputStream in = new FileInputStream(file);
        final InputStreamReader in1 = new InputStreamReader(in);
        return new BufferedReader(in1);
    }
}
