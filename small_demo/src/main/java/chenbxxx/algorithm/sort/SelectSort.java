package chenbxxx.algorithm.sort;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;

import java.util.Objects;

/**
 * 选择排序
 *
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/10/12
 */
public class SelectSort {

    /**
     * 选择排序是我觉得在众多排序中逻辑最简单的一个,
     * 每次仅需把剩下的数字中的最小或最大的一个数交换到最前面
     *
     * @param arr
     */
    void sSort(int[] arr) {
        if (Objects.isNull(arr) || arr.length == 0) {
            return;
        }

        for (int i = 0; i < arr.length; i++) {
            int sign = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[sign] > arr[j]) {
                    sign = j;
                }
            }
            if (sign != i) {
                swap(arr, i, sign);
            }
        }
    }

    /**
     * 交换数组中两数位置
     *
     * @param arr 对应数组
     * @param src 原位置
     * @param des 目标位置
     */
    void swap(int[] arr, int src, int des) {
        int i = arr[src];
        arr[src] = arr[des];
        arr[des] = i;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{3, 2, 4, 5, 6, 1};
        new SelectSort().sSort(arr);

        for (int i : arr) {
            System.out.print(i + " ");
        }
    }
}
