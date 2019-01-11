package chen.easy;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-11
 */
public class LeetCode509 {
    class Solution {
        public int fib(int N) {
            if(N < 2){
                return N;
            }
            if(N == 2){
                return 1;
            }
            int f1 = 1,f2 = 1,result =0;
            for(int i = 3;i <= N;i++){
                result = f1 + f2;
                f1 = f2;
                f2 = result;
            }
            return result;
        }
    }
}
