package graph;

import java.util.*;

public class GraphGenerator {
    private final Random random;

    public GraphGenerator(long seed) {
        this.random = new Random(seed);
    }

    public GraphGenerator() {
        this.random = new Random();
    }

    public Graph generate(int vertexCount, int edgeCount, int requiredCount, int minCost, int maxCost) {
        validateParameters(vertexCount, edgeCount, requiredCount, minCost, maxCost);

        Graph graph = new Graph();

        for (int i = 0; i < vertexCount; i++) {
            graph.addVertex(i);
        }

        Set<String> usedEdges = new HashSet<>();

        // Добавляем цикл, чтобы граф был сильно связным:
        for (int i = 0; i < vertexCount; i++) {
            int from = i;
            int to = (i + 1) % vertexCount;
            int cost = random.nextInt(maxCost - minCost + 1) + minCost;

            graph.addEdge(from, to, cost, false);
            usedEdges.add(edgeKey(from, to));
        }

        // Добавляем остальные случайные дуги
        while (graph.edgeCount() < edgeCount) {
            int from = random.nextInt(vertexCount);
            int to = random.nextInt(vertexCount);

            if (from == to) {
                continue;
            }

            String key = edgeKey(from, to);
            if (usedEdges.contains(key)) {
                continue;
            }

            int cost = random.nextInt(maxCost - minCost + 1) + minCost;
            graph.addEdge(from, to, cost, false);
            usedEdges.add(edgeKey(from, to));
        }

        // 3. Выбираем обязательные дуги
        List<Edge> edges = new ArrayList<>(graph.getEdges());
        Collections.shuffle(edges, random);

        for (int i = 0; i < requiredCount; i++) {
            edges.get(i).isRequired = true;
        }

        return graph;
    }

    private String edgeKey(int from, int to) {
        return from + "->" + to;
    }

    private void validateParameters(
            int vertexCount,
            int edgeCount,
            int requiredCount,
            int minCost,
            int maxCost
    ) {
        if (vertexCount < 2) {
            throw new IllegalArgumentException("Вершин должно быть не менее 2");
        }

        if (edgeCount < vertexCount) {
            throw new IllegalArgumentException(
                    "Количество дуг должно быть не меньше количества вершин"
            );
        }

        if (requiredCount < 1) {
            throw new IllegalArgumentException("Обязательных дуг должно быть не менее 1");
        }

        if (requiredCount > edgeCount) {
            throw new IllegalArgumentException("Обязательных дуг должно быть не больше чем самих дуг");
        }

        if (minCost <= 0) {
            throw new IllegalArgumentException("Минимальная стоимость дуги не может быть отрицательной");
        }

        if (maxCost < minCost) {
            throw new IllegalArgumentException("Максимальная стоимость дуги должна быть больше минимальной");
        }
    }
}
