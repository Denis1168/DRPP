package solver;

import com.jom.OptimizationProblem;
import graph.Edge;
import graph.Graph;
import graph.Vertex;
import graph.algorithms.RequiredComponentsFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solver {

    public static SolverResult solve(Graph graph) {
        List<Edge> requiredEdges = new ArrayList<>(graph.getRequiredEdges());
        List<Edge> nonRequiredEdges = graph.getEdges().stream().filter(e -> !e.isRequired).toList();

        int nRequired = requiredEdges.size();
        int nNonRequired = nonRequiredEdges.size();

        long modelStart = System.nanoTime();
        OptimizationProblem op = new OptimizationProblem();

        // Переменные
        op.addDecisionVariable("x", true, new int[] {1, nRequired}, 0, Integer.MAX_VALUE);
        op.addDecisionVariable("y", true, new int[] {1, nNonRequired}, 0, Integer.MAX_VALUE);

        // Константы
        double fixedCost = 0;

        double[] costRequired = new double[nRequired];
        for (int i = 0; i < nRequired; i++) {
            Edge reqEdge = requiredEdges.get(i);
            costRequired[i] = reqEdge.cost;

            fixedCost += reqEdge.cost * (1 + reqEdge.r);
        }
        op.setInputParameter("cReq", costRequired, "row");
        op.setInputParameter("fixedCost", fixedCost);

        double[] costNonRequired = new double[nNonRequired];
        for (int i = 0; i < nNonRequired; i++) {
            costNonRequired[i] = nonRequiredEdges.get(i).cost;
        }
        op.setInputParameter("cNonReq", costNonRequired, "row");

        // Целевая функция
        op.setObjectiveFunction(
                "minimize",
                "sum(cReq .* x) + sum(cNonReq .* y) + fixedCost"
        );

        // Ограничение 1
        for (Vertex vertex : graph.getVertices()) {
            String expression1 = buildExpression1Left(requiredEdges, nonRequiredEdges, vertex);
            String expression2 = buildExpression1Right(requiredEdges, nonRequiredEdges, vertex);

            op.addConstraint(expression1 + " == " + expression2);
        }

        // Ограничения 2a и 2b
        List<Set<Vertex>> components = RequiredComponentsFinder.find(graph);
        int nComp = components.size();

        if (nComp > 20) {
            throw new IllegalArgumentException("Слишком много компонент связности" + nComp);
        }

        for (long mask = 1; mask < (1L << nComp) - 1; mask++) {
            Set<Vertex> vertexSubset = new HashSet<>();

            for (int i = 0; i < nComp; i++) {
                if ((mask & (1L << i)) != 0) {
                    vertexSubset.addAll(components.get(i));
                }
            }

            String[] expressions = buildExpression2(vertexSubset, nonRequiredEdges);

            op.addConstraint(expressions[0] + " >= 1");
            op.addConstraint(expressions[1] + " >= 1");
        }
        long modelEnd = System.nanoTime();
        double modelBuildTime = (modelEnd - modelStart) / 1_000_000_000.0;

        String glpkLibrary = System.getenv("GLPK_LIBRARY");

        if (glpkLibrary == null || glpkLibrary.isBlank()) {
            glpkLibrary = "src/main/resources/glpk-4.65/w64/glpk_4_65.dll";
        }

        // Решение
        long solveStart = System.nanoTime();
        op.solve(
                "glpk",
                "solverLibraryName",
                glpkLibrary,
                "maxSolverTimeInSeconds",
                120.0
        );

        long solveEnd = System.nanoTime();
        double opSolveTime = (solveEnd - solveStart) / 1_000_000_000.0;

        if (!op.solutionIsFeasible()) {
            throw new RuntimeException("No feasible solution found");
        }

        if (!op.solutionIsOptimal()) {
            System.out.println("Warning: feasible solution found, but optimality is not proven");
        }

        return new SolverResult(graph, (int) Math.round(op.getOptimalCost()), op.getPrimalSolution("x"), op.getPrimalSolution("y"), opSolveTime, modelBuildTime);
    }

    private static String buildExpression1Left(List<Edge> reqEdges, List<Edge> nonReqEdges, Vertex vertex) {
        StringBuilder expr = new StringBuilder();

        int cnt = 0;
        for (int i = 0; i < reqEdges.size(); i++) {
            if (reqEdges.get(i).from.id == vertex.id) {
                expr.append("(");
                expr.append(reqEdges.get(i).r);
                expr.append(" + ");
                expr.append("x(0,").append(i).append(")");
                expr.append(")");
                expr.append(" + ");
                cnt++;
            }
        }

        for (int i = 0; i < nonReqEdges.size(); i++) {
            if (nonReqEdges.get(i).from.id == vertex.id) {
                expr.append("y(0,").append(i).append(")");
                expr.append(" + ");
            }
        }
        expr.append(cnt);

        return expr.toString();
    }

    private static String buildExpression1Right(List<Edge> reqEdges, List<Edge> nonReqEdges, Vertex vertex) {
        StringBuilder expr = new StringBuilder();

        int cnt = 0;
        for (int i = 0; i < reqEdges.size(); i++) {
            if (reqEdges.get(i).to.id == vertex.id) {
                expr.append("(");
                expr.append(reqEdges.get(i).r);
                expr.append(" + ");
                expr.append("x(0,").append(i).append(")");
                expr.append(")");
                expr.append(" + ");
                cnt++;
            }
        }

        for (int i = 0; i < nonReqEdges.size(); i++) {
            if (nonReqEdges.get(i).to.id == vertex.id) {
                expr.append("y(0,").append(i).append(")");
                expr.append(" + ");
            }
        }
        expr.append(cnt);

        return expr.toString();
    }

    private static String[] buildExpression2(Set<Vertex> vertexSubset, List<Edge> nonReqEdges) {
        StringBuilder expr1 = new StringBuilder();
        StringBuilder expr2 = new StringBuilder();

        for (int i = 0; i < nonReqEdges.size(); i++) {
            Edge edge = nonReqEdges.get(i);
            if (vertexSubset.contains(edge.from) && !vertexSubset.contains(edge.to)) {
                expr1.append("y(0,").append(i).append(")");
                expr1.append(" + ");
            } else if (!vertexSubset.contains(edge.from) && vertexSubset.contains(edge.to)) {
                expr2.append("y(0,").append(i).append(")");
                expr2.append(" + ");
            }
        }

        if (expr1.isEmpty()) {
            expr1.append(0);
        } else {
            expr1.delete(expr1.length() - 3, expr1.length());
        }

        if (expr2.isEmpty()) {
            expr2.append(0);
        } else {
            expr2.delete(expr2.length() - 3, expr2.length());
        }

        return new String[] {expr1.toString(), expr2.toString()};
    }
}
