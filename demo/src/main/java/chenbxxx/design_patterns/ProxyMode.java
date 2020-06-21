package chenbxxx.design_patterns;

import java.lang.reflect.*;

/**
 * @author chen
 * @date 2020/6/17 下午12:02
 */
public class ProxyMode {

    /**
     * 抽象主题
     */
    interface FinancialManagement {
        void investmentFund(float money);
    }

    /**
     * 实际主题
     */
    static class RealFinancialManagement implements FinancialManagement {
        @Override
        public void investmentFund(float money) {
            System.out.println("亏不死你!");
        }
    }

    /**
     * 代理类
     */
    class FundManager implements FinancialManagement {

        RealFinancialManagement realFinancialManagement = new RealFinancialManagement();

        @Override
        public void investmentFund(float money) {
            realFinancialManagement.investmentFund(money);
        }
    }


}
