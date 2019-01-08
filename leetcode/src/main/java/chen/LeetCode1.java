package chen;

import java.util.Arrays;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-6
 */
public class LeetCode1 {
    static class Solution {
        public int[] twoSum(int[] nums, int target) {
            if(nums.length <= 0){
                return null;
            }
            int diff;
            for(int i =0;i < nums.length;i++){
                diff = target - nums[i];
                for(int j = i+1;j < nums.length;j++){
                    if(diff == nums[j]){
                        return new int[]{i, j};
                    }
                }
            }
            return null;
        }
    }


    public static void main(String[] args) {
        int[] ints = {1, 2, 3, 4, 5, 6, 7};
        Arrays.stream(new Solution().twoSum(ints, 13)).forEach(System.out::println);
    }
}
