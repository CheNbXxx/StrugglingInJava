package chen.medium;

import java.util.Objects;

/**
 * @author chen
 * @description
 * @email ai654778@vip.qq.com
 * @date 19-1-6
 */
public class LeetCode2 {
    /**
     * Definition for singly-linked list.
     * public class ListNode {
     *     int val;
     *     ListNode next;
     *     ListNode(int x) { val = x; }
     * }
     */

    static class ListNode{
        int val;
        ListNode next;
        ListNode(int x){
            this.val = x;
        }
    }
    static class Solution {
        public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
            ListNode head = new ListNode(-1);
            ListNode temp = head;
            // 进位标记
            int carry = 0;
            while (Objects.nonNull(l1) || Objects.nonNull(l2)){
                // 考虑列表不同长度问题
                int num1 = l1 == null ? 0 : l1.val;
                int num2 = l2 == null ? 0 : l2.val;

                int sum = num1 + num2 + carry;
                carry = sum / 10;

                temp = temp.next = new ListNode(sum % 10);

                // 循环问题
                l1 = l1 == null ? null : l1.next;
                l2 = l2 == null ? null : l2.next;
            }
            if(carry > 0){
                temp.next = new ListNode(carry);
            }
            return head.next;
        }
    }
}
