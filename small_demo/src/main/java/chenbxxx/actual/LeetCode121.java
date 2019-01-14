package chenbxxx.actual;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 12:45
 */
public class LeetCode121 {
    static class Solution {
        public int maxProfit(int[] prices) {
            if(prices.length == 0){
                return 0;
            }
            int pLen = prices.length;
            int max = 0;
            int[] dpSign = new int[pLen];
            dpSign[0] = 0;

            for (int i = 1;i < pLen;i++){
                // 到第i天为止，利润最高的 负的也算
                dpSign[i] = prices[i] - prices[i-1] + (dpSign[i-1]>0?dpSign[i - 1]:0);
                max = Math.max(dpSign[i] ,max);
            }
            return max;
        }
    }

    public static void main(String[] args) {
        new Solution().maxProfit(new int[]{7,1,5,3,6,4});
    }
}
