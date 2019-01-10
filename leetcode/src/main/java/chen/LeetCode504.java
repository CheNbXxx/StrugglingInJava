package chen;

import lombok.extern.slf4j.Slf4j;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/10 11:43
 */
@Slf4j
public class LeetCode504 {
    static class Solution {
        private static final int BASE_NUM = 7;
        public String convertToBase7(int num) {
            int flag = 1;
            if(num < 0){
                num = -num;
                flag = -1;
            }
            int result = 0,level=0;
            while (num >= BASE_NUM){
                result += num % 7  * (Math.pow(10,level) > 0 ? Math.pow(10,level) : 1);
                num = num  / 7;
                level++;
            }
            return String.valueOf(flag * (num > 0 ? (int)(num * Math.pow(10,level) + result) : num)) ;
        }
    }

    public static void main(String[] args) {
        log.info(new Solution().convertToBase7(1));
    }
}
