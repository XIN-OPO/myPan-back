package com.algorithm.tree;

//搜索二叉树
public class IsBST {
    public static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int data){
            this.value=data;
        }
    }

    public static boolean isBST(Node x){
        return process(x).isBST;
    }

    public static class  ReturnData{
        public boolean isBST;
        public int min;
        public int max;

        public ReturnData(boolean isBST,int min ,int max){
            isBST=isBST;
            min=min;
            max=max;
        }
    }

    public static ReturnData process(Node x){
        if(x==null){
            return null;
        }
        ReturnData leftData=process(x.left);
        ReturnData rightData=process(x.right);
        int min= x.value;
        int max=x.value;
        if(leftData!=null){
            min=Math.min(min,leftData.min);
            max=Math.max(max, leftData.max);
        }
        if(rightData!=null){
            min=Math.min(min,rightData.min);
            max=Math.max(max, rightData.max);
        }
        boolean isBST=true;
        //左数不为空且左树已经不是搜索二叉树或者左边的最大值大于等于此节点的值
        if(leftData!=null && (!leftData.isBST || leftData.max>=x.value)){
            isBST=false;
        }
        if(rightData!=null && (!rightData.isBST || x.value>= rightData.min)){
            isBST=false;
        }
        return new ReturnData(isBST,min,max);
    }


}
