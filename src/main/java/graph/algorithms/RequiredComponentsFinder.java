package graph.algorithms;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

import java.util.*;

public class RequiredComponentsFinder {

    public static List<Set<Vertex>> find(Graph graph) {
        List<Set<Vertex>> components = new ArrayList<>();
        Set<Vertex> visited = new HashSet<>();

        for (Vertex start : graph.getVertices()) {
            if (visited.contains(start)) {
                continue;
            }

            if (!hasRequiredIncidentEdge(start)) {
                continue;
            }

            Set<Vertex> component = new HashSet<>();
            Queue<Vertex> queue = new ArrayDeque<>();

            visited.add(start);
            queue.add(start);

            while (!queue.isEmpty()) {
                Vertex curr = queue.poll();
                component.add(curr);

                for (Edge edge : curr.outgoingEdges) {
                    if (!edge.isRequired) {
                        continue;
                    }

                    Vertex next = edge.to;

                    if (!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                }

                for (Edge edge : curr.incomingEdges) {
                    if (!edge.isRequired) {
                        continue;
                    }

                    Vertex next = edge.from;

                    if (!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    private static boolean hasRequiredIncidentEdge(Vertex v) {
        for (Edge edge : v.outgoingEdges) {
            if (edge.isRequired) {
                return true;
            }
        }
        for (Edge edge : v.incomingEdges) {
            if (edge.isRequired) {
                return true;
            }
        }
        return false;
    }
}
