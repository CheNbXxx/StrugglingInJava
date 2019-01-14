package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

/**
 * @author CheNbXxx
 * @description
 *      这道题看到第一眼就可以转化为斐波那契数列的问题,如果f(n)表示到第n级楼梯的方法，那么有f(n)=f(n-1)+f(n-2)
 * @email chenbxxx@gmail.con
 * @date 2019/1/11 14:51
 */
@Slf4j
public class LeetCode70 {
    static class Solution {
        public int climbStairs(int n) {
            if(n <= 2){
                return n;
            }
            int f1 = 1,f2 = 2,result = 0;

            for(int i = 3;i <= n;i++){
                result = f1 + f2;
                f1 = f2;
                f2 = result;
            }
            return result;
        }
    }

    public static void main(String[] args) {
        log.info(new Solution().climbStairs(4)+"");
    }
}
