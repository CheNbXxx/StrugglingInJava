package chenbxxx.actual;

import java.util.Stack;

/**
 * @author CheNbXxx
 * @description
 *       题目的意思是使用栈实现队列，栈是FILO的而队列是FIFO的，
 * @email chenbxxx@gmail.con
 * @date 2019/1/14 9:48
 */
public class LeetCode232 {

    class MyQueue {
        private Stack<Integer> stack,tran;

        /** Initialize your data structure here. */
        public MyQueue() {
            stack = new Stack<>();
            tran = new Stack<>();
        }

        /** Push element x to the back of queue. */
        public void push(int x) {
            // 将stack的元素全部压入过渡用的tran中
            while (!stack.isEmpty()){
                tran.push(stack.pop());
            }
            // 元素x入栈
            tran.push(x);
            // 重新压入stack，此时x就在stack的栈底
            while (!tran.isEmpty()){
                stack.push(tran.pop());
            }
        }

        /** Removes the element from in front of queue and returns that element. */
        public int pop() {
            if(stack.isEmpty()){
                return -1;
            }
            return stack.pop();
        }

        /** Get the front element. */
        public int peek() {
            if(stack.isEmpty()){
                return -1;
            }
            return stack.peek();
        }

        /** Returns whether the queue is empty. */
        public boolean empty() {
            return stack.isEmpty();
        }
    }

/**
 * Your MyQueue object will be instantiated and called as such:
 * MyQueue obj = new MyQueue();
 * obj.push(x);
 * int param_2 = obj.pop();
 * int param_3 = obj.peek();
 * boolean param_4 = obj.empty();
 */
}
