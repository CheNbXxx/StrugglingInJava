package chen;

import java.util.Arrays;
import java.util.List;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/14
 *
 *      题目地址:  https://leetcode-cn.com/problems/matchsticks-to-square/description/
 */
public class LeetCode473 {
    // 已知最优解
//    class Solution {
//        public boolean makesquare(int[] nums) {
//            if (nums == null || nums.length < 4) {
//                return false;
//            }
//            int sum = 0;
//            for (int i : nums) {
//                sum += i;
//            }
//            if (sum % 4 != 0) return false;
//            boolean[] isVisit=new boolean[nums.length];
//            return dfs(nums,isVisit,0,0,sum/4,sum/4);
//        }
//        private boolean dfs(int nums[],boolean[] isVist,int index,int matchEdge,int target,int edgeTarget) {
//            if(target==0){
//                return dfs(nums,isVist,0,matchEdge+1,edgeTarget,edgeTarget);
//            }
//            if(matchEdge==4){
//                return true;
//            }
//            int j=0;
//            for (int i = index; i < nums.length; i++) {
//                if(!isVist[i] && nums[i]<=target){
//                    isVist[i]=true;
//                    if(dfs(nums,isVist,i+1,matchEdge,target-nums[i],edgeTarget)) {
//                        isVist[i]=false;
//                        return true;
//                    }else for(j=i+1; j<nums.length && nums[j]==nums[i];j++);
//                    isVist[i]=false;
//                    i=j;
//                }
//            }
//            return false;
//        }
//    }
    class Solution {
        public boolean makesquare(int[] nums) {
            int length = nums.length;
            // 排除特殊情况
            if (length < 4){
                return false;
            }
            // 求长度和
            int sum = 0;
            for (int num :nums){
                sum+=num;
            }
            // 排除不能整除的情况
            if(sum % 4 != 0){
                return false;
            }
            // 求边长
            int border = sum / 4;
            int[] target = new int[4];
            return squareHandler(target,nums,0,border);
        }

        /**
         * 尝试迭代拼凑
         * @param target        目标每个边
         * @param nums          元数组
         * @param currIndex     原数组第几个
         * @param border        边长
         * @return
         */
        private boolean squareHandler(int[] target,int[] nums,int currIndex,int border){
            if(currIndex == nums.length){
                // 防止数组下标越界
                return target[0] == border && target[1] == border && target[2] == border && target[3] == border;
            }
            for (int i = 0;i < 4;i++){
                if(target[i] + nums[currIndex] <= border){
                    // 小于就尝试相加拼凑
                    target[i] += nums[currIndex];
                    // 继续拼凑下一个数
                    if(squareHandler(target,nums,currIndex+1,border)){
                        return true;
                    }
                    // 拼凑不成功则回退删除
                    target[i] -= nums[currIndex];
                }
            }
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(new LeetCode473().new Solution().makesquare(new int[]{1,2,2,2,1}));
    }
}
