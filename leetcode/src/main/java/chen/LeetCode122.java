package chen;

import lombok.extern.slf4j.Slf4j;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/12
 *
 * 题目地址:  https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-ii/description/
 *      ... 就算后一个数比前一个数大的总和就ok
 */
@Slf4j
public  class LeetCode122 {
    class Solution {
        public int maxProfit(int[] prices) {
            // 利润
            int profit = 0;

            // i为当天
            for (int i = 0;i < prices.length - 1;i++){
                // 有利润
                if(prices[i + 1] > prices[i]){
                    profit += (prices[i + 1] - prices[i]);
                }
            }
            return profit;
        }
    }

    public static void main(String[] args) {
        System.out.println(new LeetCode122().new Solution().maxProfit(new int[]{1,2,3,4,5}));
    }
}


