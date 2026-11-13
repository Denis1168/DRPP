package reducers;

import graph.Edge;
import graph.Graph;
import graph.Vertex;
import graph.algorithms.FloydWarshall;
import graph.algorithms.FloydWarshallResult;
import graph.algorithms.RequiredComponentsFinder;

import java.util.*;

public class ChristofidesReducer {

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

        boolean changed = true;
        while (changed) {
            changed = false;

            // Утверждение 1
            List<Set<Vertex>> components = RequiredComponentsFinder.find(reducedGraph);
            int nComp = components.size();

            if (nComp > 1 && nComp <= 20) {
                for (int mask = 1; mask < (1 << nComp) - 1; mask++) {
                    Set<Vertex> vertexSubset = new HashSet<>();

                    for (int i = 0; i < nComp; i++) {
                        if ((mask & (1 << i)) != 0) {
                            vertexSubset.addAll(components.get(i));
                        }
                    }

                    Edge edge = hasOneNonRequiredOutgoingEdge(reducedGraph, vertexSubset);
                    if (edge != null) {
                        edge.isRequired = true;
                        changed = true;
                    }
                }
            } else if (nComp > 20) {
                for (Set<Vertex> comp : components) {
                    Edge edge = hasOneNonRequiredOutgoingEdge(reducedGraph, comp);
                    if (edge != null) {
                        edge.isRequired = true;
                        changed = true;
                    }
                }
            }


            // Утверждение 2
            for (Vertex vertex : vertReducedGraph) {
                if (vertex.outgoingEdges.size() == 1) {
                    Edge edge = vertex.outgoingEdges.getFirst();
                    if (!edge.isRequired) {
                        edge.isRequired = true;
                        changed = true;
                    }
                }

                if (vertex.incomingEdges.size() == 1) {
                    Edge edge = vertex.incomingEdges.getFirst();
                    if (!edge.isRequired) {
                        edge.isRequired = true;
                        changed = true;
                    }
                }
            }
        }

        // Утверждение 3
        for (Vertex vertex : vertReducedGraph) {
            if (vertex.outDegree() == 1) {
                Edge edge = vertex.outgoingEdges.getFirst();

                if (edge.isRequired) {
                    int r = Math.max(0, vertex.requiredInDegree() - 1);
                    edge.r = Math.max(edge.r, r);
                }
            }

            if (vertex.inDegree() == 1) {
                Edge edge = vertex.incomingEdges.getFirst();

                if (edge.isRequired) {
                    int r = Math.max(0, vertex.requiredOutDegree() - 1);
                    edge.r = Math.max(edge.r, r);
                }
            }
        }

        return reducedGraph;
    }

    private static Edge hasOneNonRequiredOutgoingEdge(Graph graph, Set<Vertex> component) {
        List<Edge> outgoing = new ArrayList<>();

        for (Edge edge : graph.getEdges()) {
            if (!edge.isRequired
                    && component.contains(edge.from) && !component.contains(edge.to)) {
                outgoing.add(edge);
            }
        }

        if (outgoing.size() == 1) {
            return outgoing.getFirst();
        }

        return null;
    }
}
