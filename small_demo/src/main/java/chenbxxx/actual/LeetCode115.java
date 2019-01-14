package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 10:29
 */
@Slf4j
public class LeetCode115 {
    static class Solution {
        public int numDistinct(String s, String t) {
            int sLen = s.length(),tLen = t.length();
            int[][] sign = new int[sLen+1][tLen+1];
            // 直接将s和t转化为char[]
            char[] sChar = s.toCharArray(),tChar = t.toCharArray();

            // 赋值二维数组首行为1
            for (int i = 0;i <= sLen;i++){
                sign[i][0] = 1;
            }

            for (int i = 1;i <= sLen;i++){
                for (int j = 1;j <= tLen;j++){
                    sign[i][j] = sign[i-1][j];
                    if(sChar[i-1] == tChar[j-1]){
                        sign[i][j] += sign[i-1][j-1];
                    }
                }
            }
            return sign[sLen][tLen];
        }
    }


    public static void main(String[] args) {
        log.info(new Solution().numDistinct("babgbag","bag")+"");
    }


}
