package chenbxxx.algorithm.sort;


/**
 * 快速排序
 * 1. 迭代循环
 *
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/10/11
 */
public class QuickSort {

    /**
     * 采用迭代方式进行的快速排序
     *
     * @param array 待排序的数组
     * @param low   需要排序的范围最左端
     * @param high  需要排序的范围最右端
     */
    private void qSort(int[] array, int low, int high) {
        if (low < high) {
            int pivot = innerSort(array, low, high);
            qSort(array, low, pivot - 1);
            qSort(array, pivot + 1, high);
        }
    }

    /**
     * 具体的片段排序方法
     *
     * @param array 待排序的数组
     * @param low   需要排序的范围最左端
     * @param high  需要排序的范围最右端
     * @return
     */
    private int innerSort(int[] array, int low, int high) {
        // 选取一个基准点
        // 数组中的所有数会和该数比较,比他大的放在右边,小的放在左边
        int sign = array[low];
        while (low < high) {
            // 先从右往左
            while (low < high && array[high] > sign) {
                --high;
            }
            array[low] = array[high];

            while (low < high && array[low] < sign) {
                ++low;
            }
            array[high] = array[low];
        }
        array[low] = sign;
        return low;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{3, 4, 1, 2, 7, 5, 6};
        new QuickSort().qSort(arr, 0, arr.length - 1);
        for (int i : arr) {
            System.out.print(i + " ");
        }
    }
}
