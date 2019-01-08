package chen;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-6
 */
public class LeetCode3 {
    static class Solution {

        /**
         * 考虑滑动窗口机制
         * @param s
         * @return
         */
        public int lengthOfLongestSubstring(String s) {
            if (Objects.isNull(s) || s.length() == 0){
                return 0;
            }
            byte[] bytes = s.getBytes();
            int maxSign = 1;
            int sign = 1;
            int begin = 0;
            outer:
            for (int i = 1;i < bytes.length;i++){
                int tempSign = sign;
                inner:
                for (int j = begin;j < begin+tempSign;j++){
                    // 相等时重置记录参数
                    if(bytes[i] == bytes[j]){
                        maxSign = maxSign > sign ? maxSign : sign;
                        sign = 1;
                        i = ++begin;
                        continue outer;
                    }
                }
                sign++;
            }
            maxSign = maxSign > sign ? maxSign : sign;
            return maxSign;
        }

        /**
         * 优化的滑动窗口机制,一直往后遍历就行了 O(n)的时间复杂度
         *      map记录char和下标,如果找到一样的替换之前的下标
         * @param s
         * @return
         */
        public int better(String s) {
            int n = s.length(), ans = 0;
            Map<Character, Integer> map = new HashMap<>();
            for (int j = 0, i = 0; j < n; j++) {
                if (map.containsKey(s.charAt(j))) {
                    i = Math.max(map.get(s.charAt(j)), i);
                }
                ans = Math.max(ans, j - i + 1);
                map.put(s.charAt(j), j + 1);
            }
            return ans;
        }
    }

    public static void main(String[] args) {
        System.out.println(new Solution().better("dvdf"));
    }
}
