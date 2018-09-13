package chen;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/13
 *      题目地址：https://leetcode-cn.com/problems/palindrome-linked-list/description/
 *      TODO: 看懂0ms大神答案
 */
public class LeetCode234 {
    // 0ms大神答案
//    class Solution {
//        public boolean isPalindrome(ListNode head) {
//            /*0ms大神解法*/
//            if (head == null || head.next == null) return true;//空链表和长度1的链表返回true
//            if (head.next.next == null) {
//                if (head.val == head.next.val) return true;
//                return false;
//            }
//            //长度3以上的链表比较
//            ListNode slow = head;
//            ListNode fast = head.next;
//            while (fast.next != null) {
//                if (fast.next.val == slow.val) {
//                    if (fast.next.next != null) return false;
//                    fast.next = null;
//                    slow = slow.next;
//                    fast = slow.next;
//                    if (fast == null || fast.val == slow.val) {
//                        return true;
//                    }
//                } else {
//                    fast = fast.next;
//                }
//            }
//            return false;
//        }
//    }

    class Solution {
        public boolean isPalindrome(ListNode head) {
            // 排除一个节点或无节点的情况
            if (head == null || head.next == null){
                return true;
            }

            // 两个节点的情况
            if(head.next.next == null){
                return head.val == head.next.val;
            }
            // 转换成List
            List<Integer> array = new ArrayList<>();

            do {
                array.add(head.val);
            }while ((head = head.next) != null);
            return checkPalindrome(array);
        }

        private boolean checkPalindrome(List list){
            int size = list.size();
            for (int i = 0;i < size / 2;i++){
                if(!list.get(i).equals(list.get(size - i - 1))){
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        ListNode head = new ListNode(-129);
        ListNode node1 = new ListNode(-129);
        head.next = node1;
//        ListNode node2 = new ListNode(1);
//        node1.next = node2;
//        ListNode node3 = new ListNode(1);
//        node2.next = node3;
//        ListNode node4 = new ListNode(1);
//        node3.next = node4;
//        ListNode node5 = new ListNode(1);
//        node4.next = node5;
        System.out.println(new LeetCode234().new Solution().isPalindrome(head));
    }
}

class ListNode {
    final int val;
    ListNode next;
    ListNode(int x) {
        val = x;
    }
}
