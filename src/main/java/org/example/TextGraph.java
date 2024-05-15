package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.imageio.ImageIO;
import javax.swing.*;
//B2 change
public class TextGraph {
    private Map<String, Set<Edge>> graph;  //一个映射，也就是一个字符串映射到一个edge类的集合，该edge类有两个属性，顶点名字已经该条边的权重，如下
    private static class Edge {
        String vertex;
        int weight;

        Edge(String vertex, int weight) {
            this.vertex = vertex;
            this.weight = weight;
        }
    }
    public TextGraph(){
        this.graph = new HashMap<>();
    }
    void showDirectedGraph(Map<String, Set<Edge>> G) throws IOException {
        Graph<String, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        G.keySet().forEach(graph::addVertex);   // 构造顶点
        for (Map.Entry<String, Set<Edge>> entry : G.entrySet()) { //构造边
            String key = entry.getKey();
            if(!entry.getValue().isEmpty()){
                for (Edge edge : entry.getValue()) {
                    DefaultWeightedEdge edge1 = graph.addEdge(key, edge.vertex);
                    // 设置权重
                    if(edge1!=null){
                        graph.setEdgeWeight(edge1,edge.weight);
                    }
                }
            }
        }
        // 将有向图写入文件
        JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<String, DefaultWeightedEdge>(graph);
        graphAdapter.setEdgeLabelsMovable(true);

        // 添加权重标签
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            String weightLabel = String.valueOf(graph.getEdgeWeight(edge));
            graphAdapter.getEdgeToCellMap().get(edge).setValue(weightLabel);
        }
        //生成png文件
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 1.3, Color.WHITE, true, null);
        File imgFile = new File("src/main/resources/graph.png");
        ImageIO.write(image,"PNG", imgFile);
        // 显示png文件
        Image imageShow = ImageIO.read(new File("src/main/resources/graph.png"));
        JFrame frame = new JFrame("Show Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700); // 设置窗口大小
        // 创建一个JLabel来显示图片
        JLabel label = new JLabel(new ImageIcon(image));
        // 将JLabel添加到JFrame中
        frame.getContentPane().add(label);
        // 显示窗口
        frame.setVisible(true);

    }
    String queryBridgeWords(String word1, String word2){
        // 如果word1和word2不存在
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            if(!graph.containsKey(word1) && !graph.containsKey(word2)) return "No " + "\"" + word1 + "\"" + " and " + "\"" + word2 + "\"" + " in the graph!";
            return !graph.containsKey(word1)? "No " + "\"" + word1 + "\"" + " in the graph!":"No " + "\"" + word2 + "\"" + " in the graph!";
        }
        // 查找word1的所有后继节点
        Set<Edge> successorsOfWord1 = graph.get(word1);
        Set<String> bridgeWords = new HashSet<>();
        for (Edge edge : successorsOfWord1) {
            String potentialBridgeWord = edge.vertex;
            Set<Edge> successorsOfBridgeWord = graph.get(potentialBridgeWord);

            if (successorsOfBridgeWord != null && successorsOfBridgeWord.stream().anyMatch(e -> e.vertex.equals(word2))) {
                //System.out.println(successorsOfBridgeWord.stream());
                bridgeWords.add(potentialBridgeWord);
            }
        }
        // 根据找到的桥接词数量返回结果
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + "\"" + word1 + "\"" + " to " + "\"" + word2 + "\"" + "!";
        } else {
            StringBuilder sb = new StringBuilder("The bridge words from " + "\"" + word1 + "\"" + " to " + "\"" + word2 + "\"" + " are: ");
            for (String bridgeWord : bridgeWords) {
                sb.append(bridgeWord).append(", ");
            }
            // 移除最后的逗号和空格
            if (sb.length() > 2) {
                sb.setLength(sb.length() - 2);
            }
            return sb.toString();
        }
    }
    String generateNewText(String inputText){
        return null;
    }
    String calcShortestPath(String word1, String word2){
        return null;
    }
    String randomWalk(){
        return null;
    }
    void readFile(String filePath){   // 读取文件并且处理语句
        File file = new File(filePath);
        String text = "";   //用来记录处理后的语句，都变为小写，且用空格分隔
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // 逐行读取文件
            while ((line = br.readLine()) != null) {
                // 使用正则表达式替换标点符号和非字母字符为空格
                line = line.replaceAll("[^a-zA-Z\\s]", " ");
                // 将换行符和回车符转换为空格
                line = line.replaceAll("\\s+", " ");
                // 将所有字母都替换为小写
                line = line.toLowerCase();
                text += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(text);
        String[] words = text.split("\\s+");    // 将text分割为字符串数组
        // 遍历单词数组，构建有向边
        int i;
        for (i = 0; i < words.length - 1; i++) {
            String currentWord = words[i];
            String nextWord = words[i + 1];
            Set<Edge> edges = graph.get(currentWord);
            //如果当前单词没有后继节点的set集合，则创建一个新的
            if(edges==null){
                graph.put(currentWord,new HashSet<>());
                graph.get(currentWord).add(new Edge(nextWord,1));
            }else {     // 如果已经有set集合，就要在set集合里寻找是否已经存在nextword，如果存在只需要将weight+1，不存在则需要新创建
                boolean has = false;
                for (Edge edge : edges) {
                    if(edge.vertex.equals(nextWord)){
                        edge.weight++; // 增加权重
                        has = true;
                        break;
                    }
                }
                if(!has){
                    edges.add(new Edge(nextWord,1));
                }
            }
        }
        graph.computeIfAbsent(words[i],k -> new HashSet<>());   // 处理最后一个单词，如果已经有set集合则不需要创建，没有需要创建
//        for (Map.Entry<String, Set<Edge>> entry : graph.entrySet()) {
//            String vertex = entry.getKey();
//            Set<Edge> edges = entry.getValue();
//            System.out.println("Vertex: " + vertex);
//            for (Edge edge : edges) {
//                System.out.println("  -> " + edge.vertex + " (weight: " + edge.weight + ")");
//            }
//        }
    }
    public static void main(String[] args) throws IOException {
        TextGraph textGraph = new TextGraph();  //实例化一个类
        textGraph.readFile("src/main/java/org/example/test.txt");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nSelect a feature to perform an action:");
            System.out.println("1. Show Directed Graph");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate New Text");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Perform Random Walk");
            System.out.println("0. Exit");
            System.out.print("Enter your choice (0-5): ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    textGraph.showDirectedGraph(textGraph.graph);
                    break;
                case 2:
                    System.out.print("Enter word1: ");
                    String word1 = scanner.next();
                    System.out.print("Enter word2: ");
                    String word2 = scanner.next();
                    System.out.println(textGraph.queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.print("Enter new text: ");
                    String inputText = scanner.nextLine();
                    System.out.println(textGraph.generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("Enter word1: ");
                    word1 = scanner.next();
                    System.out.print("Enter word2: ");
                    word2 = scanner.next();
                    System.out.println(textGraph.calcShortestPath(word1, word2));
                    break;
                case 5:
                    System.out.println(textGraph.randomWalk());
                    break;
                case 0:
                    System.out.println("Exiting program.");
                    scanner.close();
                    System.exit(0);
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number between 0 and 5.");
                    break;
            }
        }
    }
}
