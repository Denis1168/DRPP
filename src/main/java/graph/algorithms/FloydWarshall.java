package graph.algorithms;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

import java.util.*;

public class FloydWarshall {
    private static final int INF = 1_000_000_000;

    public static FloydWarshallResult run(Graph graph) {
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Comparator.comparingInt(v -> v.id));

        int n = vertices.size();

        Map<Integer, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            idToIndex.put(vertices.get(i).id, i);
        }

        int[][] dist = new int[n][n];
        int[][] next = new int[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], INF);
            Arrays.fill(next[i], -1);

            dist[i][i] = 0;
            next[i][i] = i;
        }

        for (Edge edge : graph.getEdges()) {
            int from = idToIndex.get(edge.from.id);
            int to = idToIndex.get(edge.to.id);

            if (edge.cost < dist[from][to]) {
                dist[from][to] = edge.cost;
                next[from][to] = to;
            }
        }

        for (int k = 0; k < n; k++) {
            for(int i = 0; i < n; i++) {
                if (dist[i][k] == INF) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == INF) {
                        continue;
                    }

                    int newDist = dist[i][k] + dist[k][j];
                    if (dist[i][j] > newDist) {
                        dist[i][j] = newDist;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
        return new FloydWarshallResult(dist, next, vertices, idToIndex);
    }
}
