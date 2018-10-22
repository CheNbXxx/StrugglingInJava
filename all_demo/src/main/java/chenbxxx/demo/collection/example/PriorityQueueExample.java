package chenbxxx.demo.collection.example;

import java.util.PriorityQueue;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/26
 */
public class PriorityQueueExample extends PriorityQueue<PriorityQueueExample.ToDoList> {

    static class ToDoList{
        private int primary;
        private int secondary;
        private int value;

        public ToDoList(int primary, int secondary, int value) {
            this.primary = primary;
            this.secondary = secondary;
            this.value = value;
        }

        @Override
        public String toString() {
            return primary + "&" + secondary + ":" + value;
        }
    }

    public static void main(String[] args) {

        PriorityQueueExample priorityQueueExample = new PriorityQueueExample();
        priorityQueueExample.add(new ToDoList(1,1,10));
        priorityQueueExample.add(new ToDoList(1,0,20));
        priorityQueueExample.add(new ToDoList(2,1,10));
        priorityQueueExample.add(new ToDoList(1,1,10));
        priorityQueueExample.add(new ToDoList(3,1,40));

        while (priorityQueueExample.isEmpty()){
            System.out.println(priorityQueueExample.remove());
        }
    }
}
