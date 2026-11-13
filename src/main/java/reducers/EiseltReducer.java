package reducers;

import graph.Edge;
import graph.Graph;
import graph.Vertex;
import graph.algorithms.FloydWarshall;
import graph.algorithms.FloydWarshallResult;
import visualization.DotExporter;

import java.util.ArrayList;
import java.util.List;

public class EiseltReducer {

    public static Graph reduce(Graph graph) {
        FloydWarshallResult distances = FloydWarshall.run(graph);

        Graph reducedGraph = new Graph();

        for (Edge edge : graph.getEdges()) {
            if (edge.isRequired) {
                reducedGraph.addEdge(edge.from.id, edge.to.id, edge.cost, true);
            }
        }

        List<Vertex> vertReducedGraph = new ArrayList<>(reducedGraph.getVertices());
        int n = vertReducedGraph.size();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    int fromId = vertReducedGraph.get(i).id;
                    int toId = vertReducedGraph.get(j).id;

                    if (!distances.hasPath(fromId, toId)) {
                        continue;
                    }

                    int cost = distances.getDistance(fromId, toId);

                    boolean hasSameCost = vertReducedGraph.get(i).outgoingEdges.stream()
                            .anyMatch(e -> e.to.id == toId && e.isRequired && e.cost == cost);

                    boolean hasBetter = false;
                    for (int k = 0; k < n; k++) {
                        if (k != i && k != j) {
                            int kId = vertReducedGraph.get(k).id;

                            if (!distances.hasPath(fromId, kId) || !distances.hasPath(kId, toId)) {
                                continue;
                            }

                            int distIJ = distances.getDistance(fromId, toId);
                            int distIK = distances.getDistance(fromId, kId);
                            int distKJ = distances.getDistance(kId, toId);
                            if (distIJ >= distIK + distKJ) {
                                hasBetter = true;
                                break;
                            }
                        }
                    }

                    if (!hasSameCost && !hasBetter) {
                        reducedGraph.addEdge(fromId, toId, cost, false);
                    }
                }
            }
        }

        return reducedGraph;
    }
}
