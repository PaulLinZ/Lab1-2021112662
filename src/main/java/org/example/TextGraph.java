package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;


import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxStylesheet;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.FastLookupGraphSpecificsStrategy;

import javax.imageio.ImageIO;
import javax.swing.*;

public class TextGraph {
    private Map<String, Set<Edge>> graph;  //一个映射，也就是一个字符串映射到一个edge类的集合，该edge类有两个属性，顶点名字以及该条边的权重，如下
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
    void showDirectedGraph(Map<String, Set<Edge>> G, String... shortestPath) throws IOException {
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
        // 如果传入了 shortestPath 参数，则突出显示最短路径
        if (shortestPath.length > 0) {
            // 突出显示最短路径上的边
            String[] pathEdges = shortestPath[0].split("->");
            for (int i = 0; i < pathEdges.length - 1; i++) {
                String source = pathEdges[i];
                String target = pathEdges[i + 1];
                DefaultWeightedEdge edge = graph.getEdge(source, target);
                if (edge != null) {
                    mxStylesheet stylesheet = graphAdapter.getStylesheet();
                    Map<String, Object> highlightedEdgeStyle = new HashMap<>();
                    highlightedEdgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#FF0000"); // 将最短路径的边设置为红色
                    highlightedEdgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 3.0f); // 加粗边的宽度
                    stylesheet.putCellStyle("highlightedEdgeStyle", highlightedEdgeStyle); // 将样式添加到样式表
                    graphAdapter.getEdgeToCellMap().get(edge).setStyle("highlightedEdgeStyle");
                }
            }
            mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
            layout.execute(graphAdapter.getDefaultParent());
            BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 1.3, Color.WHITE, true, null);
            JFrame frame = new JFrame("Show Graph");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 700); // 设置窗口大小
            // 创建一个JLabel来显示图片
            JLabel label = new JLabel(new ImageIcon(image));
            // 将JLabel添加到JFrame中
            frame.getContentPane().add(label);
            // 显示窗口
            frame.setVisible(true);
            return;
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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        // 按空格分割成单词数组
        String[] inputWords = inputText.split("\\s+");
        StringBuilder newText = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < inputWords.length - 1; i++) {
            // 将当前单词添加到新文本
            newText.append(inputWords[i]).append(" ");
            // 查询bridge word
            String bridgeWord = queryBridgeWords(inputWords[i].toLowerCase(), inputWords[i + 1].toLowerCase());
            // 如果存在bridge word，随机选择一个插入
            if (!bridgeWord.isEmpty() && !bridgeWord.startsWith("No")) {
                String[] bridgeWords = bridgeWord.substring(bridgeWord.indexOf(":") + 2).split(", ");
                int randomIndex = random.nextInt(bridgeWords.length); // 随机索引
                newText.append(bridgeWords[randomIndex]).append(" "); // 插入随机选择的bridge word
            }
        }
        // 添加最后一个单词到新文本
        newText.append(inputWords[inputWords.length - 1]);

        // 返回生成的新文本
        return newText.toString();
    }
    String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + "\"" + word1 + "\"" + " or " + "\"" + word2 + "\"" + " in the graph!";
        }

        // 使用 HashMap 存储每个节点的最短路径估计和前驱节点
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();

        // 初始化所有节点的最短路径估计为无穷大，源节点为 0
        for (String word : graph.keySet()) {
            distances.put(word, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);

        // 使用 PriorityQueue 存储所有待处理的节点
        Queue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        queue.add(word1);

        // Dijkstra 算法主循环
        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(word2)) {
                break; // 找到目标节点，结束循环
            }

            for (Edge edge : graph.getOrDefault(current, Collections.emptySet())) {
                String neighbor = edge.vertex;
                int distanceThroughCurrent = distances.get(current) + edge.weight;

                // 如果通过当前节点到达邻居节点的路径更短，则更新该路径
                if (distanceThroughCurrent < distances.get(neighbor)) {
                    distances.put(neighbor, distanceThroughCurrent);
                    predecessors.put(neighbor, current); // 更新前驱节点

                    // 重新将邻居节点加入队列，因为我们更新了它的最短路径估计
                    queue.add(neighbor);
                }
            }
        }

        // 回溯构建最短路径字符串
        StringBuilder path = new StringBuilder();
        String current = word2;
        while (current != null) {
            path.insert(0, current + "->");
            current = predecessors.get(current);
        }

        // 移除最后的 "->" 并添加源节点
        path.delete(path.length() - "->".length(), path.length());

        // 如果源节点和目标节点相同，或者没有路径连接它们
        if (!path.toString().startsWith(word1)) {
            return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
        }

        return path.toString();
    }
    volatile boolean stopRequested = false;
    String randomWalk() {
        if (graph.isEmpty()) {
            return "Graph is empty";
        }
        // 从图中随机选择一个起始节点
        Random random = new Random();
        Set<String> nodes = graph.keySet();
        final String[] current = {new ArrayList<>(nodes).get(random.nextInt(nodes.size()))};
        // 记录访问过的边
        Set<Edge> visitedEdges = new HashSet<>();
        List<String> walkPath = new ArrayList<>();
        Thread printThread = new Thread(() -> {
            try {
                stopRequested = false;
                System.out.println("Input \"s\" to stop!");
                while (true) {
                    // 添加当前节点到路径
                    walkPath.add(current[0]);
                    System.out.print(current[0]);
                    // 获取当前节点的所有出边
                    Set<Edge> edges = graph.get(current[0]);
                    if (edges.isEmpty()) {
                        // 当前节点没有出边，结束游走
                        System.out.println("\r");
                        break;
                    }
                    // 随机选择一条出边
                    Edge chosenEdge = edges.stream().skip(random.nextInt(edges.size())).findFirst().orElse(null);
                    // 如果选择的出边为空或者选择的出边已访问，则结束游走
                    if (chosenEdge == null) {
                        break;
                    }
                    else if (visitedEdges.contains(chosenEdge)) {
                        current[0] = chosenEdge.vertex;
                        System.out.print("->");
                        System.out.println(current[0]);
                        walkPath.add(current[0]);
                        break;
                    }
                    System.out.print("->");
                    // 添加选定的边到访问过的边集合中
                    visitedEdges.add(chosenEdge);
                    // 更新当前节点为选定边的目标节点
                    current[0] = chosenEdge.vertex;
                    Thread.sleep(2000); // 等待2秒
                }
            } catch (InterruptedException e) {
                // 当等待过程中被中断，不进行任何操作
            } finally {
                stopRequested = true;
                System.out.println("Traverse finished!");
            }
        });
        Thread inputThread = new Thread(() -> {
            try {
                while (!stopRequested) {
                    // 持续检查是否有输入可读取，不会阻塞主线程的执行
                    if (System.in.available() > 0) {
                        Scanner scanner = new Scanner(System.in);
                        if (scanner.nextLine().equalsIgnoreCase("s")) {
                            // 如果用户输入了 "s"，则中断等待打印的线程
                            printThread.interrupt();
                        }
                    }
                    Thread.sleep(100); // 等待一段时间再检查是否有输入
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });


        printThread.start();
        inputThread.start();
        try {
            // 等待两个线程执行完毕
            printThread.join();
            inputThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 构建并返回游走路径的字符串表示
        return String.join(" ", walkPath);
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
                text = text + line + " ";
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

    }
    // 根据两个单词获取边的权重
    private int calculateEdgeWeight(String startWord, String endWord) {
        if (graph.containsKey(startWord)) {
            for (Edge edge : graph.get(startWord)) {
                if (edge.vertex.equals(endWord)) {
                    return edge.weight;
                }
            }
        }
        return 0;
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        TextGraph textGraph = new TextGraph();  //实例化一个类
        textGraph.readFile("src/main/java/org/example/test.txt");
        Scanner scanner = new Scanner(System.in);
        boolean firstTag = false;
        while (true) {
            if(firstTag){
                Thread.sleep(2000);
            }
            firstTag = true;
            System.out.println("\nSelect a feature to perform an action:");
            System.out.println("1. Show Directed Graph");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate New Text");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Perform Random Walk");
            System.out.println("0. Exit");
            System.out.print("Enter your choice (0-5): ");
            while (!scanner.hasNextInt()) {
                System.out.println("请输入一个整数:");
                scanner.next(); // 清除输入缓冲区
            }
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
                    scanner.nextLine(); // 清除换行符
                    String inputText = scanner.nextLine();
                    System.out.println(textGraph.generateNewText(inputText));
                    break;
                case 4:
                    // 询问用户是否输入第一个单词
                    System.out.print("Do you want to enter the first word? (yes/no): ");
                    String userInput = scanner.next();
                    String startWord, endWord;

                    if (userInput.equalsIgnoreCase("yes")) {
                        System.out.print("Enter word1: ");
                        startWord = scanner.next();
                    } else if (userInput.equalsIgnoreCase("no")) {
                        startWord = ""; // 为空表示以 endWord 为起点
                    } else {
                        System.out.println("Invalid choice. Please enter 'yes' or 'no'.");
                        break;
                    }

                    // 如果用户输入了第一个单词，则询问是否输入第二个单词
                    if (!startWord.isEmpty()) {
                        System.out.print("Do you want to enter the second word? (yes/no): ");
                        userInput = scanner.next();

                        if (userInput.equalsIgnoreCase("yes")) {
                            System.out.print("Enter word2: ");
                            endWord = scanner.next();
                        } else if (userInput.equalsIgnoreCase("no")) {
                            endWord = ""; // 为空表示以 startWord 为终点
                        } else {
                            System.out.println("Invalid choice. Please enter 'yes' or 'no'.");
                            break;
                        }
                    } else {
                        // 如果用户没有输入第一个单词，则询问是否输入第二个单词
                        System.out.print("Do you want to enter the second word? (yes/no): ");
                        userInput = scanner.next();

                        if (userInput.equalsIgnoreCase("yes")) {
                            System.out.print("Enter word2: ");
                            endWord = scanner.next();
                        } else if (userInput.equalsIgnoreCase("no")) {
                            System.out.println("No Word1 and Word2.");
                            break;
                        } else {
                            System.out.println("Invalid choice. Please enter 'yes' or 'no'.");
                            break;
                        }
                    }

                    // 如果两个输入的单词中有一个为空，则以不为空的单词为源或目标顶点
                    String sourceWord = startWord.isEmpty() ? endWord : startWord;
                    String targetWord = endWord.isEmpty() ? startWord : endWord;

                    // 如果 startWord 不空，endWord 为空，则以 startWord 为源顶点，遍历除了 startWord 之外的所有顶点
                    if (!startWord.isEmpty() && endWord.isEmpty()) {
                        if (!textGraph.graph.containsKey(sourceWord)) {
                            System.out.println("No " + "\"" + sourceWord + "\"" + " in the graph!");
                        }
                        else {
                            for (String vertex : textGraph.graph.keySet()) {
                                if (!vertex.equals(startWord)) {
                                    // 以每个顶点作为目标顶点调用 calcShortestPath 函数计算最短路径
                                    String shortestPath = textGraph.calcShortestPath(sourceWord, vertex);
                                    System.out.println(shortestPath);

                                    // 如果路径存在，展示最短路径和最短路径长度
                                    if (!shortestPath.startsWith("No")) {
                                        textGraph.showDirectedGraph(textGraph.graph, shortestPath);
                                        String[] pathWords = shortestPath.split("->");
                                        int pathLength = 0;
                                        String previousWord = sourceWord.toLowerCase();

                                        // 遍历路径中的单词，累加权重
                                        for (String currentWord : pathWords) {
                                            currentWord = currentWord.toLowerCase();
                                            pathLength += textGraph.calculateEdgeWeight(previousWord, currentWord);
                                            previousWord = currentWord;
                                        }

                                        System.out.println("The length of the shortest path from \"" + sourceWord + "\" to \"" + vertex + "\" is: " + pathLength);
                                    }
                                }
                            }
                        }
                    }
                    // 如果 startWord 为空，endWord 不空，则以 endWord 为源顶点，遍历除了 endWord 之外的所有顶点
                    else if (startWord.isEmpty() && !endWord.isEmpty())
                    {
                        if (!textGraph.graph.containsKey(targetWord)) {
                            System.out.println("No " + "\"" + targetWord + "\"" + " in the graph!");
                        }
                        else {
                            for (String vertex : textGraph.graph.keySet()) {
                                if (!vertex.equals(endWord)) {
                                    // 以每个顶点作为目标顶点调用 calcShortestPath 函数计算最短路径
                                    String shortestPath = textGraph.calcShortestPath(targetWord, vertex);
                                    System.out.println(shortestPath);

                                    // 如果路径存在，展示最短路径和最短路径长度
                                    if (!shortestPath.startsWith("No")) {
                                        textGraph.showDirectedGraph(textGraph.graph, shortestPath);
                                        String[] pathWords = shortestPath.split("->");
                                        int pathLength = 0;
                                        String previousWord = targetWord.toLowerCase();

                                        // 遍历路径中的单词，累加权重
                                        for (String currentWord : pathWords) {
                                            currentWord = currentWord.toLowerCase();
                                            pathLength += textGraph.calculateEdgeWeight(previousWord, currentWord);
                                            previousWord = currentWord;
                                        }

                                        System.out.println("The length of the shortest path from \"" + targetWord + "\" to \"" + vertex + "\" is: " + pathLength);
                                    }
                                }
                            }
                        }
                    }
                    // 如果 startWord 和 endWord 都不空，则以 startWord 为源顶点，endword 作为目标顶点
                    else {
                        // 计算并展示最短路径，并打印最短路径长度
                        String shortestPath = textGraph.calcShortestPath(sourceWord, targetWord);
                        System.out.println(shortestPath);

                        // 如果路径存在，展示最短路径和最短路径长度
                        if (!shortestPath.startsWith("No")) {
                            textGraph.showDirectedGraph(textGraph.graph, shortestPath);
                            String[] pathWords = shortestPath.split("->");
                            int pathLength = 0;
                            String previousWord = sourceWord.toLowerCase();

                            // 遍历路径中的单词，累加权重
                            for (String currentWord : pathWords) {
                                currentWord = currentWord.toLowerCase();
                                pathLength += textGraph.calculateEdgeWeight(previousWord, currentWord);
                                previousWord = currentWord;
                            }

                            System.out.println("The length of the shortest path from \"" + sourceWord + "\" to \"" + targetWord + "\" is: " + pathLength);
                        }
                    }
                    break;

                case 5:
                    String randomWalkResult = textGraph.randomWalk();
                    //System.out.println(randomWalkResult);
                    // 将随机游走结果写入文件
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/example/random_walk_path.txt"));
                        writer.write(randomWalkResult);
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred while writing to the file.");
                        e.printStackTrace();
                    }
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
