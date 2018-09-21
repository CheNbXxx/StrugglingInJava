package chenbxxx.demo.collection;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/21
 */
public class StackExample {

    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);


        System.out.println(stack.peek());
        int search = stack.search(2);
        System.out.println(search);

        Iterator<Integer> iterator = stack.iterator();

        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}

