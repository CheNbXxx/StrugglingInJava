package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 15:11
 */
@Slf4j
public class LeetCode198 {
    static class Solution {
        public int rob(int[] nums) {
            if(nums.length == 0){
                return 0;
            }
            if(nums.length == 1){
                return nums[0];
            }
            int nLen = nums.length;
            int[] dpSign = new int[nLen];

            dpSign[0] = nums[0];
            dpSign[1] = Math.max(nums[0],nums[1]);
            int max = dpSign[1];

            for (int i = 2;i < nLen;i++){
                dpSign[i] = Math.max(dpSign[i-1],dpSign[i-2]+nums[i]);
                max = Math.max(dpSign[i],max);
            }
            return max;

        }
    }

    public static void main(String[] args) {
        log.info(""+new Solution().rob(new int[]{2,7,9,3,1}));
    }
}
