package graph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    public final int id;
    public final List<Edge> outgoingEdges = new ArrayList<>();
    public final List<Edge> incomingEdges = new ArrayList<>();

    public Vertex(int id) {
        this.id = id;
    }

    public int outDegree() {
        return outgoingEdges.size();
    }

    public int inDegree() {
        return incomingEdges.size();
    }

    public int requiredOutDegree() {
        int cnt = 0;
        for (Edge edge : outgoingEdges) {
            if (edge.isRequired) {
                cnt++;
            }
        }
        return cnt;
    }

    public int requiredInDegree() {
        int cnt = 0;
        for (Edge edge : incomingEdges) {
            if (edge.isRequired) {
                cnt++;
            }
        }
        return cnt;
    }
}
