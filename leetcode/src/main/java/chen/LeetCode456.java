package chen;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/13
 * @version 2018/9/13 没有太好的思路,先放着
 *
 *  题目地址:   https://leetcode-cn.com/problems/132-pattern/description/
 *
 *  TODO: 1
 */
public class LeetCode456 {
    class Solution {
        public boolean find132pattern(int[] nums) {
            int length;
            if(nums == null || (length = nums.length) < 3){
                return false;
            }

            // 保存每个位置上的最小值
            int[] min = new int[length];
            min[0] = nums[0];

            // 计算最小值
            for (int i = 1;i < length;i++){
                min[i] = Math.min(min[i-1],nums[i]);
            }

            for (int i = length - 1; i > 0;i--){
                //
                if(nums[i] > min[i - 1]){

                }
            }
            return false;
        }
    }
}
