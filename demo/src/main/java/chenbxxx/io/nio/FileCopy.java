package chenbxxx.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FileChannelDemo
 *
 * @author chen
 * @date 19 -3-23
 */
@Slf4j
public class FileCopy {
    /**
     * 固定缓冲区大小
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        File srcFile = new File("/home/chen/temp"), desFile = new File("/home/chen/test");
        new FileCopy().direct(srcFile, desFile);
    }

    /**
     * 普通复制,从一个Channel中读取数据到Buffer,在从Buffer写入到另外一个Channel
     *
     * @param srcFile 源文件
     * @param desFile 目标文件
     * @throws IOException 复制时异常
     */
    private void normal(File srcFile, File desFile) throws IOException {
        checkAndCreate(desFile);
        try (FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
             FileChannel desChannel = new FileOutputStream(desFile).getChannel()) {
            // 采用堆外内存
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            while (srcChannel.read(byteBuffer) > 0) {
                // 翻转,从写模式改为读模式
                byteBuffer.flip();
                desChannel.write(byteBuffer);
                byteBuffer.flip();
                byteBuffer.rewind();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用数据转移方法进行零拷贝复制
     *
     * @param srcFile 源文件
     * @param desFile 目标文件
     * @throws IOException 复制时异常
     */
    private void direct(File srcFile, File desFile) throws IOException {
        checkAndCreate(desFile);
        try (FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
             FileChannel desChannel = new FileOutputStream(desFile).getChannel()) {
            srcChannel.transferTo(0, srcChannel.size(), desChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查并在未创建时创建文件
     *
     * @param desFile   目标文件
     * @return  文件存在 | 创建成功
     * @throws IOException  创建异常
     */
    private boolean checkAndCreate(File desFile) throws IOException {
        return desFile.exists() || desFile.createNewFile();
    }
}
