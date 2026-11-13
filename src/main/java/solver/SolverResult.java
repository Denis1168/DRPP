package solver;

import com.jom.DoubleMatrixND;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

public class SolverResult {
    private final Graph graph;
    private final int optimalCost;
    private final DoubleMatrixND xSolution;
    private final DoubleMatrixND ySolution;
    private final double opSolveTime;
    private final double modelBuildTime;

    public SolverResult(Graph graph, int optimalCost, DoubleMatrixND xSolution, DoubleMatrixND ySolution, double opSolveTime, double modelBuildTime) {
        this.graph = graph;
        this.optimalCost =  (int) optimalCost;
        this.xSolution = xSolution;
        this.ySolution = ySolution;
        this.opSolveTime = opSolveTime;
        this.modelBuildTime = modelBuildTime;
    }

    public void show() {
        List<Edge> requiredEdges = new ArrayList<>(graph.getRequiredEdges());
        List<Edge> nonRequiredEdges = graph.getEdges().stream().filter(e -> !e.isRequired).toList();

        int nRequired = requiredEdges.size();
        int nNonRequired = nonRequiredEdges.size();

        System.out.println("Solve time: " + (opSolveTime + modelBuildTime) + " с.");
        System.out.println("Objective value: " + optimalCost);
        System.out.println("\nRequired edges:");

        for (int i = 0; i < nRequired; i++) {
            Edge edge = requiredEdges.get(i);

            double value = xSolution.get(new int[] {0, i});
            int xValue = (int) Math.round(value);

            int totalPasses = 1 + edge.r + xValue;

            System.out.println(
                    edge.from.id + " -> " + edge.to.id +
                            ", cost = " + edge.cost +
                            ", r = " + edge.r +
                            ", x = " + xValue +
                            ", total = " + totalPasses
            );
        }

        System.out.println("\nNon-required edges:");

        for (int i = 0; i < nNonRequired; i++) {
            Edge edge = nonRequiredEdges.get(i);

            double value = ySolution.get(new int[] {0, i});
            int yValue = (int) Math.round(value);

            if (yValue > 0) {
                System.out.println(
                        edge.from.id + " -> " + edge.to.id +
                                ", cost = " + edge.cost +
                                ", y = " + yValue
                );
            }
        }
    }

    public int getOptimalCost() {
        return optimalCost;
    }

    public double getOpSolveTime() {
        return opSolveTime;
    }

    public double getModelBuildTime() {
        return modelBuildTime;
    }

    public double getModelSolveTime() {
        return modelBuildTime + opSolveTime;
    }

    public DoubleMatrixND getXSolution() {
        return xSolution;
    }

    public DoubleMatrixND getYSolution() {
        return ySolution;
    }
}
