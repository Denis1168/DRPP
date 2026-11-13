package graph;

import java.util.*;

public class Graph {
    private final Map<Integer, Vertex> vertices = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private int edgeCnt = 0;

    public Vertex addVertex(int id) {
        if (vertices.containsKey(id)) {
            return vertices.get(id);
        }

        Vertex v = new Vertex(id);
        vertices.put(id, v);
        return v;
    }

    public Edge addEdge(int fromId, int toId, int cost, boolean isRequired) {
        Vertex from = addVertex(fromId);
        Vertex to = addVertex(toId);
        Edge edge = new Edge(edgeCnt++, from, to, cost, isRequired);

        from.outgoingEdges.add(edge);
        to.incomingEdges.add(edge);
        edges.add(edge);

        return edge;
    }

    public void removeEdge(Edge edge) {
        edge.from.outgoingEdges.remove(edge);
        edge.to.incomingEdges.remove(edge);
        edges.remove(edge);
    }

    public Set<Vertex> getRequiredVertexes() {
        Set<Vertex> reqVert = new HashSet<>();
        for (Edge edge : edges) {
            if (edge.isRequired) {
                reqVert.add(edge.from);
                reqVert.add(edge.to);
            }
        }
        return reqVert;
    }

    public List<Edge> getRequiredEdges() {
        List<Edge> requiredEdges = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.isRequired) {
                requiredEdges.add(edge);
            }
        }

        return requiredEdges;
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vertex getVertex(int id) {
        return vertices.get(id);
    }

    public int vertexCount() {
        return vertices.size();
    }

    public int edgeCount() {
        return edges.size();
    }

    public Graph copy() {
        Graph copy = new Graph();

        for (Vertex vertex : vertices.values()) {
            copy.addVertex(vertex.id);
        }

        for (Edge edge : edges) {
            Edge newEdge = copy.addEdge(
                    edge.from.id,
                    edge.to.id,
                    edge.cost,
                    edge.isRequired
            );

            newEdge.r = edge.r;
        }

        return copy;
    }
}
