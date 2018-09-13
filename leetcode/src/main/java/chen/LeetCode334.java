package chen;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/13
 *
 *      题目地址: https://leetcode-cn.com/problems/reverse-string/description/
 *
 *      思路:
 *          1. 拆成字符数组,对半交换.
 */
public class LeetCode334 {
    class Solution {
        public String reverseString(String s) {
            if(s.length() < 0){
                return "";
            }
            char temp;
            char[] chars = s.toCharArray();
            for (int i = 0, j = s.length() - 1;i < j;i++,j--){
                temp = chars[i];
                chars[i] = chars[j];
                chars[j] = temp;
            }
            return String.valueOf(chars);
        }
    }

    public static void main(String[] args) {
        System.out.println(new LeetCode334().new Solution().reverseString("A man, a plan, a canal: Panama"));
    }
}
