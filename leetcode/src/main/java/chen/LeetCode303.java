package chen;


/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/13
 *
 *      题目地址:  https://leetcode-cn.com/problems/range-sum-query-immutable/description/
 *      初始思路：
 *          刚开始没考虑到多次调用的路子,以为就是简单地求和
 */
public class LeetCode303 {
    class NumArray {

        private int[] sums;

        /**
         * 构造函数中直接求和,存储和就好
         * @param nums
         */
        public NumArray(int[] nums) {
            if (nums.length <= 0){
                return;
            }
            sums = new int[nums.length];
            //  此处直接计算0~1 -> 0~nums.length的和
            sums[0] = nums[0];
            for (int i =1;i < nums.length;i++){
                sums[i] = sums[i-1]+nums[i];
            }
        }

        public int sumRange(int i, int j) {
            return i == 0
                    ? sums[i]
                    : sums[j] - sums[i-1];
        }
    }

    public static void main(String[] args){
        System.out.println(new LeetCode303().new NumArray(new int[]{1,2,3,4,5}).sumRange(1,2));
    }
}
