package chenbxxx.example.designpattern;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2018/11/7 16:38
 */
public class ResponsibilityChain {
    public static void main(String[] args) throws Exception {
        String msg = "责任责任责任";
        MyResponsibilityChian myResponsibilityChian = new MyResponsibilityChian()
                .addResponsibility(new ProcessTest1());

        myResponsibilityChian.doResponsibility(msg);
    }

}

/**
 * 责任链主体
 */
class MyResponsibilityChian {
    /**
     * 使用一个List存放所有需要的操作
     */
    List<Process> processes = new ArrayList<>();

    /**
     * 添加责任链
     *
     * @param process
     * @return
     */
    public MyResponsibilityChian addResponsibility(Process process) {
        processes.add(process);
        return this;
    }

    /**
     * 执行整段责任链
     *
     * @param msg
     */
    public void doResponsibility(String msg) throws Exception {
        for (Process process : processes) {
            process.doWork(msg);
        }
    }
}

/**
 * `责任`超类
 */
interface Process {
    /**
     * 执行任务方法
     *
     * @return
     * @throws Exception
     */
    void doWork(String msg) throws Exception;
}

class ProcessTest1 implements Process {
    @Override
    public void doWork(String msg) throws Exception {
        System.out.println("processTest1:" + msg);
    }
}
