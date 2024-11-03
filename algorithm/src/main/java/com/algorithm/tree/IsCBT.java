package com.algorithm.tree;

import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;

import java.util.LinkedList;

//完全二叉树
public class IsCBT {
    public static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int data){
            this.value=data;
        }
    }

    public static boolean isCBT(Node head){//completed binary tree
        if(head==null){
            return true;
        }
        LinkedList<Node> queue =new LinkedList<>();
        //是否遇到过左右两个孩子不双全的结点
        boolean leaf= false;
        Node l=null;
        Node r=null;
        queue.add(head);
        while (!queue.isEmpty()){
            head=queue.poll();
            l=head.left;
            r=head.right;
            //如果是叶子并且左右孩子有一个不是空 或者是 没有左孩子但是有右孩子 那么就不是完全二叉树
            if( (leaf && (l!=null||r!=null)) || (l==null&&r!=null)){
                return false;
            }
            if(l!=null){
                queue.add(l);
            }
            if(r!=null){
                queue.add(r);
            }
            if(l==null||r==null){
                leaf=true;
            }
        }
        return true;
    }



}
