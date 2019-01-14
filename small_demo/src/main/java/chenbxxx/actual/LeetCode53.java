package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 11:28
 */
@Slf4j
public class LeetCode53 {
    static class Solution {
        public int maxSubArray(int[] nums) {
            // 用于记录以每个元素结尾的maxSubArray
            int[] dpRes = new int[nums.length];
            int max = nums[0];
            dpRes[0] = max;

            for (int i = 1; i < nums.length;i++){
                //
                dpRes[i] = nums[i] + (dpRes[i - 1]  > 0 ? dpRes[i -1] : 0);
                max = Math.max(dpRes[i],max);
            }
            return max;
        }
    }

    public static void main(String[] args) {
        int[] i = new int[]{-2,1,-3,4,-1,2,1,-5,4};
        log.info(new Solution().maxSubArray(i)+"");
    }
}
