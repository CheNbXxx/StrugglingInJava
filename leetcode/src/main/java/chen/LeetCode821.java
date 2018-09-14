package chen;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/14
 */
public class LeetCode821 {
    class Solution {
        public int[] shortestToChar(String S, char C) {
            // 排除特殊情况
            int length = S.length();
            int[] dis = new int[length];
            // 先计算两端距离
            int firstDis = S.indexOf(C);
            for (int i = 0;i <= firstDis;i++){
                dis[i] = firstDis - i;
            }
            int lastDis = S.indexOf(C);
            for (int i = length - 1;i >= lastDis;i++){
                dis[i] = length-1-i;
            }

            /**
             * 此处注意last位置字符不包含,但first包含
             */
            String substring = S.substring(firstDis + 1, lastDis);
            return null;
        }
    }

    public static void main(String[] args) {
        String s = "aaeisuidusneaa";
        System.out.println(s.substring(s.indexOf('e'),s.lastIndexOf('e')+1));
    }
}
