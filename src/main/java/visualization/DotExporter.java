package visualization;

import com.jom.DoubleMatrixND;
import graph.Edge;
import graph.Graph;
import graph.Vertex;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import solver.SolverResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DotExporter {

    public static String toDot(Graph graph) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph G {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=circle];\n\n");

        for (Vertex vertex : graph.getVertices()) {
            sb.append("    ")
                    .append(vertex.id)
                    .append(";\n");
        }

        sb.append("\n");

        for (Edge edge : graph.getEdges()) {
            sb.append("    ")
                    .append(edge.from.id)
                    .append(" -> ")
                    .append(edge.to.id)
                    .append(" [label=\"")
                    .append(edge.cost);

            sb.append("\"");

            if (edge.isRequired) {
                sb.append(", color=red, penwidth=2");
            }

            sb.append("];\n");
        }

        sb.append("}\n");

        return sb.toString();
    }

    public static String toDotSolution(Graph graph, SolverResult result) {
        List<Edge> requiredEdges = new ArrayList<>(graph.getRequiredEdges());
        List<Edge> nonRequiredEdges = graph.getEdges()
                .stream()
                .filter(edge -> !edge.isRequired)
                .toList();

        Map<Edge, Integer> passes = new HashMap<>();

        DoubleMatrixND xSolution = result.getXSolution();
        DoubleMatrixND ySolution = result.getYSolution();

        for (int i = 0; i < requiredEdges.size(); i++) {
            Edge edge = requiredEdges.get(i);
            int x = (int) Math.round(xSolution.get(new int[] {0, i}));
            int total = 1 + edge.r + x;
            passes.put(edge, total);
        }

        for (int i = 0; i < nonRequiredEdges.size(); i++) {
            Edge edge = nonRequiredEdges.get(i);
            int y = (int) Math.round(ySolution.get(new int[] {0, i}));
            passes.put(edge, y);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("digraph G {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=circle];\n\n");

        for (Vertex vertex : graph.getVertices()) {
            sb.append("    ")
                    .append(vertex.id)
                    .append(";\n");
        }

        sb.append("\n");

        for (Edge edge : graph.getEdges()) {
            int count = passes.getOrDefault(edge, 0);
            boolean used = count > 0;

            if (used) {
                sb.append("    ")
                        .append(edge.from.id)
                        .append(" -> ")
                        .append(edge.to.id)
                        .append(" [label=\"")
                        .append(edge.cost)
                        .append(", k=")
                        .append(count)
                        .append("\"");
            } else {
                sb.append("    ")
                        .append(edge.from.id)
                        .append(" -> ")
                        .append(edge.to.id)
                        .append(" [label=\"")
                        .append(edge.cost)
                        .append("\"");
            }

            if (used) {
                sb.append(", penwidth=4");

                if (edge.isRequired) {
                    sb.append(", color=red, fontcolor=red");
                } else {
                    sb.append(", color=blue, fontcolor=blue");
                }
            } else {
                sb.append(", color=gray, fontcolor=gray, penwidth=1");
            }

            sb.append("];\n");
        }

        sb.append("}\n");

        return sb.toString();
    }

    public static void saveToDotFile(Graph graph, String fileName) {
        try {
            if (!Files.exists(Path.of("src/main/resources/graphs"))) {
                Files.createDirectory(Path.of("src/main/resources/graphs"));
            }

            Files.writeString(
                    Path.of("src/main/resources/graphs/" + fileName + ".dot"),
                    toDot(graph),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToPngFile(Graph graph, String fileName) {
        try {
            if (!Files.exists(Path.of("src/main/resources/graphs"))) {
                Files.createDirectory(Path.of("src/main/resources/graphs"));
            }

            Graphviz.fromString(toDot(graph))
                    .width(1200)
                    .render(Format.PNG)
                    .toFile(new File("src/main/resources/graphs/" + fileName + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToSvgFile(Graph graph, String fileName) {
        try {
            if (!Files.exists(Path.of("src/main/resources/graphs"))) {
                Files.createDirectory(Path.of("src/main/resources/graphs"));
            }

            Graphviz.fromString(toDot(graph))
                    .width(1200)
                    .render(Format.SVG)
                    .toFile(new File("src/main/resources/graphs/" + fileName + ".svg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSolutionToSvgFile(Graph graph, SolverResult result, String fileName) {
        try {
            if (!Files.exists(Path.of("src/main/resources/graphs"))) {
                Files.createDirectory(Path.of("src/main/resources/graphs"));
            }

            Graphviz.fromString(toDotSolution(graph, result))
                    .width(1200)
                    .render(Format.SVG)
                    .toFile(new File("src/main/resources/graphs/" + fileName + ".svg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String returnSvgFile(Graph graph) {
        return Graphviz.fromString(toDot(graph))
                .width(1200)
                .render(Format.SVG)
                .toString();
    }

    public static String returnSolutionSvg(Graph graph, SolverResult result) {
        return Graphviz.fromString(toDotSolution(graph, result))
                .width(1200)
                .render(Format.SVG)
                .toString();
    }
}
