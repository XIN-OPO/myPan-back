package com.algorithm.Graph;

public class GraphGenerator {
    //matrix 所有的边
    //N*3的矩阵
    //[from节点上的值,to节点上的值,weight,]
    public static Graph createGraph(Integer[][] matrix){
        Graph graph=new Graph();
        for (Integer[] integers : matrix) {
            Integer from = integers[0];
            Integer to = integers[1];
            Integer weight = integers[2];
            if (!graph.nodes.containsKey(from)) {
                graph.nodes.put(from, new Node(from));
            }
            if (!graph.nodes.containsKey(to)) {
                graph.nodes.put(to, new Node(to));
            }
            Node fromNode = graph.nodes.get(from);
            Node toNode = graph.nodes.get(to);

            Edge newEdge = new Edge(weight, fromNode, toNode);
            fromNode.nexts.add(toNode);
            fromNode.out++;
            toNode.in++;
            fromNode.edges.add(newEdge);
            graph.edges.add(newEdge);
        }
        return graph;
    }
}
