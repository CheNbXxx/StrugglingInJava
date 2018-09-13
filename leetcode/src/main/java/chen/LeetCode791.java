package chen;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/12
 *
 *   题目地址:   https://leetcode-cn.com/problems/custom-sort-string/description/
 *   起始思路:  将S和T都拆成char数组,遍历S的数组,按顺序提取出T中的对应数据,用一个List<char>可以保存.
 */
@Slf4j
public class LeetCode791 {
    class Solution {
        public String customSortString(String S, String T) {
            // 字符数组
            char[] chars = S.toCharArray();
            // 新建list,保存结果字符串
            List<Character> characterList = new ArrayList<Character>();
            // 遍历排序字符数组
            for (char c : chars){
                // 旧长度
                int oldLength = T.length();
                // 将String中的c全部替换为''
                T = T.replaceAll(String.valueOf(c),"");
                // 新长度
                int newLength = T.length();

                // 根据长度差添加对应个数char
                if(newLength < oldLength){
                    int i = oldLength - newLength;
                    while (i  > 0){
                        characterList.add(c);
                        i--;
                    }
                }
            }

            // 添加剩余char
            for (char c : T.toCharArray()){
                characterList.add(c);
            }

            // 按要求输出,无空格,无多余字符
            return characterList.toString().replaceAll("\\[|\\]|\\,","").replaceAll(" ","");
        }
    }

    public static void main(String[] args) {
        log.info(new LeetCode791().new Solution().customSortString("cba","abcd"));
    }
}
