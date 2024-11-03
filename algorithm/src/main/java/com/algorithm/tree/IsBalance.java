package com.algorithm.tree;

//平衡二叉树
public class IsBalance {
    public static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int data){
            this.value=data;
        }
    }

    public static boolean isFull(Node head){
        ReturnData allInfo=p(head);
        return (1<<allInfo.height-1)==allInfo.nums;
    }
    public static class ReturnData{
        public int height;
        public int nums;

        public ReturnData(int h,int n){
            height=h;
            nums=n;
        }
    }

    public static ReturnData p(Node x){
        if(x==null){
            return new ReturnData(0,0);
        }
        ReturnData leftData=p(x.left);
        ReturnData rightData=p(x.right);

        int height=Math.max(leftData.height, rightData.height)+1;

        int nums= leftData.nums+rightData.nums+1;
        return new ReturnData(height,nums);
    }

    public static boolean isBalanced(Node head){
        return process(head).isBalanced;
    }

    public static class ReturnType{
        public boolean isBalanced;
        public int height;

        public ReturnType(boolean isB,int height){
            isBalanced=isB;
            height=height;
        }
    }

    public static ReturnType process(Node x){
        if(x==null){
            return new ReturnType(true,0);
        }
        ReturnType leftData=process(x.left);
        ReturnType rightData=process(x.right);
        int height=Math.max(leftData.height,rightData.height)+1;
        boolean isBalanced=leftData.isBalanced&&rightData.isBalanced
                &&Math.abs(leftData.height- rightData.height)<2;
        return new ReturnType(isBalanced,height);
    }
}
