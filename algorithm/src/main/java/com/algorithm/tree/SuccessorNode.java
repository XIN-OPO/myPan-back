package com.algorithm.tree;


public class SuccessorNode {
    public static class Node{
        public int value;
        public Node left;
        public Node right;
        public Node parent;

        public Node(int data){
            this.value=data;
        }
    }

    //求某一个结点的后继结点 后继节点就是中序遍历中这个结点的下一个结点
    public static Node getSuccessorNode(Node node){
        if(node==null){
            return node;
        }
        if(node.right!=null){
            return getLeftMost(node.right);
        }else {
            Node parent=node.parent;
            while (parent!=null && parent.left!=node){
                node=parent;
                parent=node.parent;
            }
            return parent;        }
    }
    public static Node getLeftMost(Node node){
        if(node == null){
            return node;
        }
        while (node.left!=null){
            node=node.left;
        }
        return node;
    }
}
