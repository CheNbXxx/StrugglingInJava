package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

/**
 * @author CheNbXxx
 * @description
 *      这题唯一能和动态规划扯上关系的也就只有需要保存子过程结果了。
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 15:53
 */
@Slf4j
public class LeetCode303 {
    static class NumArray {
        int[] dpSign;
        public NumArray(int[] nums) {
            dpSign = new int[nums.length];
            if(dpSign.length == 0) {
                return;
            }
            dpSign[0] = nums[0];
            for(int i=1; i<nums.length;i++) {
                dpSign[i] = dpSign[i-1] + nums[i];
            }
        }

        public int sumRange(int i, int j) {
            return i==0? dpSign[j]:dpSign[j]-dpSign[i-1];
        }
    }

/**
 * Your NumArray object will be instantiated and called as such:
 * NumArray obj = new NumArray(nums);
 * int param_1 = obj.sumRange(i,j);
 */
}
