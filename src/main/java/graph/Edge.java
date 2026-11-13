package graph;

public class Edge {
    public final int id;
    public final Vertex from;
    public final Vertex to;
    public int cost;
    public boolean isRequired;
    public int r;

    public Edge(int id, Vertex from, Vertex to, int cost, boolean isRequired) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.cost = cost;
        this.isRequired = isRequired;
    }

    @Override
    public String toString() {
        return from.id + " -> " + to.id + " cost=" + cost + " required=" + isRequired;
    }
}
