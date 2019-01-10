package chen;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/10 12:17
 */
@Slf4j
public class LeetCode215 {
    static class Solution {
        public int findKthLargest(int[] nums, int k) {
            // 1. k是否超过数组长度的一半,超过从小往大找,未超过从大往小找
            int flag = k > nums.length / 2 ? 1 : -1;
            return bubbleSore(nums,flag > 0 ? k : nums.length+1-k,flag);
        }

        public int bubbleSore(int[] nums,int k,int flag){
            if(flag == 1) {
                for (int i = 0; i < k; i++) {
                    int max = i;
                    for (int j = i + 1; j < nums.length; j++) {
                        if (nums[max] < nums[j]) {
                            max = j;
                        }
                    }
                    swap(nums, i, max);
                }
            }else{
                for (int i = 0; i < k; i++) {
                    int min = i;
                    for (int j = i + 1; j < nums.length; j++) {
                        if (nums[min] > nums[j]) {
                            min = j;
                        }
                    }
                    swap(nums, i, min);
                }
            }
            return nums[k-1];
        }

        public void swap(int[] nums,int i,int j){
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
        }
    }

    public static void main(String[] args) {
        log.info(new Solution().findKthLargest(new int[]{3,2,1,5,6,4},2)+"");
    }
}
