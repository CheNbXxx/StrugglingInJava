package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.*;

/**
 * 利用多线程复制文件
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 18-10-26
 */
@Slf4j
public class CopyFileByMultithread {

    /**
     * 用于产生固定线程数的线程池
     */
    private ThreadPoolExecutor threadPoolExecutor;

    private int threadNum;

    /**
     * 构造函数
     * @param threadNum  最大线程数目
     */
    public CopyFileByMultithread(int threadNum) {
        this.threadNum = threadNum;
        this.threadPoolExecutor = new ThreadPoolExecutor(threadNum, threadNum, 10, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>());
    }

    public void copyFile(File srcFile, File desFile) throws IOException {
        log.info("************ Copy Starting************");
        log.info("源文件为:{}",srcFile.getAbsolutePath());
        log.info("目标文件为:{}",desFile.getAbsolutePath());

        // 判断输入参数正确性
        if(!srcFile.exists()){
            log.info("源文件不存在");
            return;
        }
        if(!desFile.exists()){
            if(!desFile.getParentFile().mkdirs() && !desFile.createNewFile()){
                log.info("目标文件创建失败");
                return;
            }
            log.info("目标文件创建成功");
        }

        RandomAccessFile srcRaf = new RandomAccessFile(srcFile,"r");
        // 计算跳过的字节数,第一个线程不跳过,第二个线程开始跳,一共跳threadNum次
        long l = srcFile.length() % threadNum;
        long byteSize = l == 0 ? srcFile.length() / threadNum: srcFile.length() / threadNum + 1;

        log.info("源文件大小为:{}",srcFile.length());
        log.info("复制线程{}个",threadNum);
        log.info("每个线程承担的字节数为:{}",byteSize);

        for (int temp = 0; temp < threadNum;temp++){
            threadPoolExecutor.execute(new CopyRunnable(temp,byteSize,srcRaf,new RandomAccessFile(desFile,"rw")));
        }
    }

    /**
     * 负责copy文件的线程类
     */
    class CopyRunnable implements Runnable{

        // 第几段数据
        private int num;

        // 跳过的字节数
        private long skipByteSize;

        // 源文件
        private RandomAccessFile srcFile;

        // 目标文件
        private RandomAccessFile desFile;

        public CopyRunnable(int num,long skipByteSize, RandomAccessFile srcFile, RandomAccessFile desFile) {
            this.num = num;
            this.skipByteSize = skipByteSize;
            this.srcFile = srcFile;
            this.desFile = desFile;
        }

        @Override
        public void run() {
            try {
                log.info("第{}段复制线程开始运行",num);
                // 创建文件随机存取类,只负责写入就够了
                srcFile.seek(num*skipByteSize);
                desFile.seek(num*skipByteSize);

                byte[] buff = new byte[1024];

                int readSize = 0;
                while ((readSize = srcFile.read(buff)) > 0){
                    log.info("读取了"+readSize);
                    desFile.write(buff,0,readSize);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public int getNum() {
            return num;
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/home/chen/test");
        File file1 = new File("/home/chen/testCopy11");

//        new CopyFileByMultithread(3).copyFile(file,file1);

        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file,true);
        byte[] bytes = "HelloWorld \n".getBytes();
        for (int j = 1; j < 1000;j++) {
            for (int i = 1; i < 100000; i++) {
                fileOutputStream.write(bytes);
            }
        }
    }
}
