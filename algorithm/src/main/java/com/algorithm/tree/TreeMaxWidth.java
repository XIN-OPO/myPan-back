package com.algorithm.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class TreeMaxWidth {

    public static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int data){
            this.value=data;
        }
    }

    //宽度优先遍历
    public static void w(Node head){
        if(head==null){
            return;
        }
        Queue<Node> queue=new LinkedList<>();
        queue.add(head);
        while (!queue.isEmpty()){
            Node cur=queue.poll();//弹出就打印，然后先放左再放右
            System.out.println(cur.value);
            if(cur.left!=null){
                queue.add(cur.left);
            }
            if(cur.right!=null){
                queue.add(cur.right);
            }
        }
    }

    //获取最大宽度
    public static void w2(Node head){
        if(head==null){
            return;
        }
        Queue<Node> queue=new LinkedList<>();
        queue.add(head);
        HashMap<Node,Integer> levelMap=new HashMap<>();
        levelMap.put(head,1);
        int curLevel=1;
        int curLevelNodes=0;
        int max=Integer.MIN_VALUE;
        while (!queue.isEmpty()){
            Node cur=queue.poll();//弹出就打印，然后先放左再放右
            int curNodeLevel=levelMap.get(cur);
            if(curNodeLevel==curLevel){
                curLevelNodes++;
            }else {
                max=Math.max(max,curLevelNodes);
                curLevel++;
                curLevelNodes=1;
            }
            if(cur.left!=null){
                levelMap.put(cur.left,curNodeLevel+1);
                queue.add(cur.left);
            }
            if(cur.right!=null){
                levelMap.put(cur.right,curNodeLevel+1);
                queue.add(cur.right);
            }
        }
    }

}
