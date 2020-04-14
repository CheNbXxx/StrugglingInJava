package chenbxxx;

import java.io.*;
import java.util.Random;

/**
 * 创建一个包含大量整数的文件
 * @author chen
 * @date 2020/4/4 下午10:45
 */
public class CreateFile {
    private static final String FILE_NAME = "bigFile";

    private static final int MAX = 1000000000;

    private static final int NUM = 10000000;

    public static void main(String[] args) throws IOException {
        File file = new File(FILE_NAME);
        if(!file.exists()){
            final boolean newFile = file.createNewFile();
            if(!newFile){
                System.exit(0);
            }
        }

        final Random random = new Random(10);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(fileOutputStream),true);
        for (int i = 0;i < NUM;i++){
            printWriter.print(random.nextInt(MAX));
            printWriter.print(',');
        }

    }
}
