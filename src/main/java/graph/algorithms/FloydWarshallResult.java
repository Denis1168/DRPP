package graph.algorithms;

import graph.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FloydWarshallResult {
    int[][] dist;
    int[][] next;
    List<Vertex> vertices;
    Map<Integer, Integer> idToIndex;

    public FloydWarshallResult(int[][] dist, int[][] next, List<Vertex> vertices, Map<Integer, Integer> idToIndex) {
        this.dist = dist;
        this.next = next;
        this.vertices = vertices;
        this.idToIndex = idToIndex;
    }

    public int getDistance(int from, int to) {
        int fromInd = idToIndex.get(from);
        int toInd = idToIndex.get(to);

        return dist[fromInd][toInd];
    }

    public boolean hasPath(int from, int to) {
        int fromInd = idToIndex.get(from);
        int toInd = idToIndex.get(to);

        return next[fromInd][toInd] != -1;
    }

    public List<Integer> getPath(int from, int to) {
        int fromInd = idToIndex.get(from);
        int toInd = idToIndex.get(to);

        if (next[fromInd][toInd] == -1) {
            return Collections.emptyList();
        }

        List<Integer> path = new ArrayList<>();
        path.add(vertices.get(fromInd).id);
        int curr = fromInd;
        while (curr != toInd) {
            curr = next[curr][toInd];
            path.add(vertices.get(curr).id);
        }

        return path;
    }
}
