package com.algorithm.tree;

import java.util.HashMap;
import java.util.HashSet;

//最低公共祖先
public class LowestCommonAncestor {
    public static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int data){
            this.value=data;
        }
    }

    public static Node lca(Node head,Node o1,Node o2){
        HashMap<Node,Node> fatherMap=new HashMap<>();
        fatherMap.put(head,head);
        process(head,fatherMap);
        HashSet<Node> set1=new HashSet<>();
        Node cur=o1;
        while (cur!=fatherMap.get(cur)){
            set1.add(cur);
            cur=fatherMap.get(cur);
        }
        set1.add(head);
        cur=o2;
        while (cur!=fatherMap.get(cur)){
            if(set1.contains(cur)){
                return cur;
            }
            cur=fatherMap.get(cur);
        }
        return head;
    }

    public static void process(Node head,HashMap<Node,Node> fatherMap){
        if(head==null){
            return;
        }
        fatherMap.put(head.left,head);
        fatherMap.put(head.right,head);
        process(head.left,fatherMap);
        process(head.right,fatherMap);
    }
}
