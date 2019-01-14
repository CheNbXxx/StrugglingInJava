package chenbxxx.actual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/14 10:02
 */
public class LeetCode107 {
    /**
     * Definition for a binary tree node.
     * public class TreeNode {
     *     int val;
     *     TreeNode left;
     *     TreeNode right;
     *     TreeNode(int x) { val = x; }
     * }
     */
    class Solution {
        class TreeNode{
            int val;
            TreeNode left;
            TreeNode right;
            TreeNode(int x){
                this.val = x;
            }
        }

        List<List<Integer>> result = new ArrayList<>();;

        public List<List<Integer>> levelOrderBottom(TreeNode root) {
            handle(root,0);
            Collections.reverse(result);
            return result;
        }

        /**
         * @param node 对应节点
         * @param l l为节点所在深度
         */
        public void handle(TreeNode node,int l) {
            if(node == null){
                return;
            }
            if(l > result.size() - 1){
                result.add(new ArrayList<>());
            }
            result.get(l).add(node.val);
            handle(node.left,l+1);
            handle(node.right,l+1);
        }
    }



}
