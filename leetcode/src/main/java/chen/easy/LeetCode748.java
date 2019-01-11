package chen.easy;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-10
 */
public class LeetCode748 {
    static class Solution {
        /**用int[26]保存`a-z`的个数 **/
        private int[] sign = new int[26];
        public String shortestCompletingWord(String licensePlate, String[] words) {
            int count = 0;
            licensePlate = licensePlate.toLowerCase();
            for (int i=0;i < licensePlate.length();i++) {
                char temp = licensePlate.charAt(i);
                if (temp >= 'a' && temp <= 'z') {
                    // 数组默认为0
                    sign[temp - 'a']++;
                    count++;
                }
            }
            String result = "";
            for (String word : words) {
                if (isMatch(word, count)) {
                    if (result.equals("") || (!result.equals("") && word.length() < result.length())) {
                        result = word;
                    }
                }
            }
            return result;
        }

        public boolean isMatch(String word,int count){
            // 记录每个单词的字母数目
            int[] sign2 = new int[26];
            int index = 0;
            for (int i = 0;i < word.length();i++){
                index = word.charAt(i) - 'a';
                if(sign2[index] < sign[index]){
                    sign2[index]++;
                    count--;
                }
                // 如果全部匹配了就直接返回
                if(count == 0){
                    return true;
                }
            }
            return false;
        }
    }
}
