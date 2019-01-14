package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 14:32
 */
@Slf4j
public class LeetCode746 {
    static class Solution {
        public int minCostClimbingStairs(int[] cost) {
            int[] dpSign = new int[cost.length];

            dpSign[0] = cost[0];
            dpSign[1] = cost[1];

            for (int i = 2;i < cost.length;i++){
                dpSign[i] = Math.min(dpSign[i-1],dpSign[i-2])+cost[i];
            }

            return Math.min(dpSign[cost.length -2],dpSign[cost.length-1]);
        }
    }

    public static void main(String[] args) {
        log.info(new Solution().minCostClimbingStairs(new int[]{1, 100, 1, 1, 1, 100, 1, 1, 100, 1})+"");
    }
}
