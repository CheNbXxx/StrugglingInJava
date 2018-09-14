package chen;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/13
 *
 *      问题地址: https://leetcode-cn.com/problems/majority-element/description/
 *      初始思路：
 *          最简单的方法就是循环两边,大于一半长度就输出。 可以在原数组去除重复,加快遍历速度
 */
public class LeetCode169 {
    class Solution {
        public int majorityElement(int[] nums) {
            int result = nums[0], count = 0;
            for (int num : nums) {
                if (count == 0) {
                    result = num;
                    count++;
                } else {
                    if (result == num) {
                        count++;
                    } else {
                        count--;
                    }
                }
            }
            return result;
        }
    }

    public static void main(String[] args) {
        System.out.println(new LeetCode169().new Solution().majorityElement(new int[]{3,3,3,3,1,2,4,5,6,7,8,9}));
    }
}
