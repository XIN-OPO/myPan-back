package com.algorithm.tree;

import java.util.Stack;

//二叉树的前中后遍历
public class PreInPosTraversal {

    public static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int data){
            this.value=data;
        }
    }

    public static void preOrderRecur(Node head){
        if(head==null){
            return;
        }
        System.out.println(head.value+" ");
        preOrderRecur(head.left);
        preOrderRecur(head.right);
    }

    public static void inOrderRecur(Node head){
        if(head==null){
            return;
        }
        inOrderRecur(head.left);
        System.out.println(head.value+" ");
        inOrderRecur(head.right);
    }

    public static void posOrderRecur(Node head){
        if(head==null){
            return;
        }
        posOrderRecur(head.left);
        posOrderRecur(head.right);
        System.out.println(head.value+" ");
    }

    public static void preOrderUnRecur(Node head){
        System.out.println("pre-order:");
        if(head!=null){
            Stack<Node> stack=new Stack<Node>();
            stack.add(head);
            while (!stack.isEmpty()){//当不为空的时候先压右再压左 因为这是栈结构 先进后出
                head=stack.pop();
                System.out.println(head.value+" ");//然后处理弹出来的结点
                if(head.right!=null){
                    stack.push(head.right);
                }
                if(head.left!=null){
                    stack.push(head.left);
                }
            }
        }
        System.out.println();
    }

    public static void postOrderUnRecur(Node head){
        System.out.println("post-order: ");
        if(head!=null){
            Stack<Node> s1=new Stack<Node>();
            Stack<Node> s2=new Stack<Node>();
            s1.push(head);
            while (!s1.isEmpty()){
                head=s1.pop();
                s2.push(head);//在需要处理的时候不处理压入一个辅助栈
                if(head.left!=null){
                    s1.push(head.left);//先左后右
                }
                if(head.right!=null){
                    s1.push(head.right);//那么出栈的时候就是先右后左 最后的整个顺序就是   左右头
                }
            }
            while (!s2.isEmpty()){
                System.out.println(s2.pop().value+ " ");
            }
        }
    }

    public static void inOrderUnRecur(Node head){
        System.out.println("in-order: ");
        if(head != null){
            Stack<Node> stack=new Stack<Node>();
            while (!stack.isEmpty() || head != null){
                if(head!=null){//一直往左走
                    stack.push(head);
                    head=head.left;
                }else {
                    head=stack.pop();
                    System.out.println(head.value+ " ");
                    head=head.right;
                }
            }
        }
        System.out.println();
    }
}
