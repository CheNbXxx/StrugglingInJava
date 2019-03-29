package chenbxxx.actual;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 求n个容量为m的数组的全排列
 *
 * @author bingxin.chen
 * @date 2019/3/27 15:01
 */
@Slf4j
public class FullArrayOfArrays {
    private List<List<Integer>> res = new ArrayList<>();

    private void handle(List<List<Integer>> a) {
        permutations(a, 0, new ArrayList<>());
    }

    /**
     * DFS + 回溯
     *
     * @param list      全列表
     * @param currSize  当前纵向Index
     * @param curr      当前排列组合
     */
    private void permutations(List<List<Integer>> list, int currSize, List<Integer> curr) {
        // 深度遍历到最后一个数组
        if (currSize == list.size()) {
            log.info("" + new ArrayList<>(curr));
            return;
        }
        // 逐层遍历并递归
        for (int i : list.get(currSize)) {
            curr.add(i);
            permutations(list, currSize + 1, curr);
            curr.remove(curr.size() - 1);
        }
    }

    public static void main(String[] args) {
        List<Integer> a = Arrays.asList(1, 2, 3);
        List<Integer> b = Arrays.asList(4, 5, 6);
        List<Integer> c = Arrays.asList(7, 8, 9);

        List<List<Integer>> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        list.add(c);
        new FullArrayOfArrays().handle(list);
    }
}
