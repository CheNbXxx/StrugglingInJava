package chen;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/13
 * @version
 *      18-9-13 ：
 *
 *      题目地址：  https://leetcode-cn.com/problems/prime-palindrome/description/
 *
 */
public class LeetCode866 {
    class Solution {
        public int primePalindrome(int N) {
            // 排除小于2的情况
            if(N <= 2){ return 2; }

            // N转化为奇数
            N = N % 2 == 0 ? N+1 : N;

            // 先求是否回文 再求素数,
            for (;N < Integer.MAX_VALUE;){

                // N -> int[]
                int length = 0;
                int[] array = new int[10];
                int i = N;
                while (i / 10 != 0){
                    array[length++] = i % 10;
                    i /= 10;
                }
                array[length++] = i;

                if(length % 2 == 0 && N != 11){
                    N = (int)Math.pow(10,length)+1;
                    continue;
                }
                if(isPalindrome(array,length) && isPrime(N)){
                    return N;
                }
                N += 2;
            }
            return -1;
        }
        /**
         * 判断是否是素数
         * @param i 待校验`int`型整数
         * @return
         */
        private boolean isPrime(int i){
            if(i < 0 || i == 1){
                return false;
            }
            for (int curr = 2 ; curr <= Math.sqrt(i);curr++){
                if(i % curr == 0){
                    return false;
                }
            }
            return true;
        }


        /**
         * 判断是否回文
         * @param i 待校验`int`型整数
         * @return
         */
        private boolean isPalindrome(int i){
            String s = String.valueOf(i);
            for (int curr = 0; curr < s.length() / 2;curr++){
                if (s.charAt(curr) != s.charAt(s.length() - curr - 1)){
                    return false;
                }

            }
            return true;
        }

        /**
         *  字符串处理效率太低,改为数组处理
         * @param nums  int数组
         * @param len   数组长度
         * @return
         */
        private boolean isPalindrome(int[] nums,int len){
            for (int j = 0; j < len/2; j++) {
                if (nums[j] != nums[len - j - 1]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println(new LeetCode866().new Solution().primePalindrome(8));
    }
}
