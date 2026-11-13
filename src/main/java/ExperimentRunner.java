import graph.Graph;
import graph.GraphGenerator;
import graph.algorithms.RequiredComponentsFinder;
import reducers.ChristofidesReducer;
import reducers.EiseltReducer;
import solver.Solver;
import solver.SolverResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExperimentRunner {
    private static final int MIN_COST = 1;
    private static final int MAX_COST = 30;
    private static final int MAX_COMPONENTS_TO_SOLVE = 18;

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("src/main/resources/results"));

        List<ExperimentCase> checkCases = List.of(
                new ExperimentCase(15, 50, 20),
                new ExperimentCase(20, 80, 35),
                new ExperimentCase(30, 140, 60),
                new ExperimentCase(40, 220, 90)
        );

        List<ExperimentCase> mainCases = List.of(
                new ExperimentCase(50, 300, 120),
                new ExperimentCase(70, 500, 200),
                new ExperimentCase(100, 800, 320),
                new ExperimentCase(150, 1300, 520),
                new ExperimentCase(200, 2000, 800),
                new ExperimentCase(300, 3500, 1400)
        );

        List<ExperimentCase> requiredCases = List.of(
                new ExperimentCase(100, 800, 80),
                new ExperimentCase(100, 800, 160),
                new ExperimentCase(100, 800, 320),
                new ExperimentCase(100, 800, 500),
                new ExperimentCase(100, 800, 650)
        );

        List<ExperimentCase> lowRequiredCases = List.of(
//                new ExperimentCase(100, 800, 20),
//                new ExperimentCase(100, 800, 40),
//                new ExperimentCase(100, 800, 60),
//                new ExperimentCase(100, 800, 80),
//                new ExperimentCase(100, 800, 100),

//                new ExperimentCase(200, 1600, 40),
//                new ExperimentCase(200, 1600, 80),
//                new ExperimentCase(200, 1600, 120),
                new ExperimentCase(200, 1600, 160),

//                new ExperimentCase(300, 3000, 60),
//                new ExperimentCase(300, 3000, 120),
                new ExperimentCase(300, 3000, 180)
        );

        List<ExperimentCase> densityCases = List.of(
//                new ExperimentCase(150, 450, 100),
//                new ExperimentCase(150, 750, 100),
//                new ExperimentCase(150, 1200, 80),
//                new ExperimentCase(150, 1800, 80),

                new ExperimentCase(300, 900, 360),
                new ExperimentCase(300, 1500, 360)
//                new ExperimentCase(300, 2400, 160),
//                new ExperimentCase(300, 3600, 160)
        );

        List<ExperimentCase> scaleHardCases = List.of(
                new ExperimentCase(200, 1800, 100),
                new ExperimentCase(300, 3000, 150),
                new ExperimentCase(400, 4500, 200)
//                new ExperimentCase(500, 6000, 250),
//                new ExperimentCase(700, 9000, 350)
        );

        int[] seeds = makeSeeds(10);

        try (CsvLogger csv = new CsvLogger(
                "src/main/resources/results/results_raw1.csv",
                "series",
                "vertices",
                "edges",
                "requiredEdges",
                "seed",
                "method",
                "originalVertices",
                "originalEdges",
                "originalRequiredEdges",
                "reducedVertices",
                "reducedEdges",
                "reducedRequiredEdges",
                "reducedNonRequiredEdges",
                "components",
                "xVariables",
                "yVariables",
                "totalVariables",
                "balanceConstraints",
                "connectivityConstraints",
                "totalConstraints",
                "reductionTimeSeconds",
                "solveTimeSeconds",
                "totalTimeSeconds",
                "objectiveValue",
                "status",
                "edgeReductionPercent",
                "vertexReductionPercent"
        )) {
            runExperiments(
                    csv,
                    "check",
                    checkCases,
                    seeds,
                    true
            );
//
//            runExperiments(
//                    csv,
//                    "main",
//                    mainCases,
//                    seeds,
//                    false
//            );
//
//            runExperiments(
//                    csv,
//                    "required",
//                    requiredCases,
//                    seeds,
//                    false
//            );

//            runExperiments(
//                    csv,
//                    "low_required",
//                    lowRequiredCases,
//                    seeds,
//                    false
//            );

            runExperiments(
                    csv,
                    "density",
                    densityCases,
                    seeds,
                    false
            );

            runExperiments(
                    csv,
                    "scale_hard",
                    scaleHardCases ,
                    seeds,
                    false
            );
        }
    }

    private static int[] makeSeeds(int count) {
        int[] seeds = new int[count];
        seeds[0] = 18;

        for (int i = 1; i < count; i++) {
            seeds[i] = 1000 + i * 17;
        }

        return seeds;
    }

    private static void runExperiments(
            CsvLogger csv,
            String series,
            List<ExperimentCase> cases,
            int[] seeds,
            boolean solveOriginal
    ) {
        for (ExperimentCase experimentCase : cases) {
            for (int seed : seeds) {
                System.out.println();
                System.out.println("Series=" + series
                        + ", vertices=" + experimentCase.vertices
                        + ", edges=" + experimentCase.edges
                        + ", required=" + experimentCase.requiredEdges
                        + ", seed=" + seed);

                GraphGenerator generator = new GraphGenerator(seed);

                Graph originalGraph = generator.generate(
                        experimentCase.vertices,
                        experimentCase.edges,
                        experimentCase.requiredEdges,
                        MIN_COST,
                        MAX_COST
                );

                if (solveOriginal) {
                    runOneExperiment(
                            csv,
                            series,
                            experimentCase,
                            seed,
                            "Original",
                            originalGraph,
                            originalGraph,
                            0.0
                    );
                }

                runReducedExperiment(
                        csv,
                        series,
                        experimentCase,
                        seed,
                        "Eiselt",
                        originalGraph
                );

                runReducedExperiment(
                        csv,
                        series,
                        experimentCase,
                        seed,
                        "Christofides",
                        originalGraph
                );
            }
        }
    }

    private static ExperimentRow runReducedExperiment(
            CsvLogger csv,
            String series,
            ExperimentCase experimentCase,
            int seed,
            String method,
            Graph originalGraph
    ) {
        long start = System.nanoTime();

        Graph reducedGraph;

        if (method.equals("Eiselt")) {
            reducedGraph = EiseltReducer.reduce(originalGraph.copy());
        } else if (method.equals("Christofides")) {
            reducedGraph = ChristofidesReducer.reduce(originalGraph.copy());
        } else {
            throw new IllegalArgumentException("Неизвестный метод: " + method);
        }

        long end = System.nanoTime();
        double reductionTimeSeconds = (end - start) / 1_000_000_000.0;

        return runOneExperiment(
                csv,
                series,
                experimentCase,
                seed,
                method,
                originalGraph,
                reducedGraph,
                reductionTimeSeconds
        );
    }

    private static ExperimentRow runOneExperiment(
            CsvLogger csv,
            String series,
            ExperimentCase experimentCase,
            int seed,
            String method,
            Graph originalGraph,
            Graph graphToSolve,
            double reductionTimeSeconds
    ) {
        int originalVertices = originalGraph.vertexCount();
        int originalEdges = originalGraph.edgeCount();
        int originalRequiredEdges = originalGraph.getRequiredEdges().size();

        int reducedVertices = graphToSolve.vertexCount();
        int reducedEdges = graphToSolve.edgeCount();
        int reducedRequiredEdges = graphToSolve.getRequiredEdges().size();
        int reducedNonRequiredEdges = reducedEdges - reducedRequiredEdges;

        int components = RequiredComponentsFinder.find(graphToSolve).size();

        int xVariables = reducedRequiredEdges;
        int yVariables = reducedNonRequiredEdges;
        int totalVariables = xVariables + yVariables;

        long balanceConstraints = reducedVertices;
        long connectivityConstraints = countConnectivityConstraints(components);
        long totalConstraints = connectivityConstraints < 0 ? -1 : balanceConstraints + connectivityConstraints;

        double solveTimeSeconds = 0.0;
        double totalTimeSeconds = 0.0;
        double objectiveValue = Double.NaN;
        String status = "NOT_SOLVED";

        if (components > MAX_COMPONENTS_TO_SOLVE) {
            status = "SKIPPED_TOO_MANY_COMPONENTS";
        } else {
            long solveStart = System.nanoTime();

            try {
                SolverResult result = Solver.solve(graphToSolve);

                solveTimeSeconds = result.getModelSolveTime();
                totalTimeSeconds = reductionTimeSeconds + solveTimeSeconds;
                objectiveValue = result.getOptimalCost();

                status = "OK";

            } catch (Exception e) {
                long solveEnd = System.nanoTime();

                solveTimeSeconds = (solveEnd - solveStart) / 1_000_000_000.0;
                totalTimeSeconds = reductionTimeSeconds + solveTimeSeconds;

                status = "FAILED: " + e.getClass().getSimpleName();
                System.out.println("Ошибка в методе " + method + ": " + e.getMessage());
            }
        }

        double edgeReductionPercent = percentReduction(originalEdges, reducedEdges);
        double vertexReductionPercent = percentReduction(originalVertices, reducedVertices);

        ExperimentRow row = new ExperimentRow(
                series,
                experimentCase.vertices,
                experimentCase.edges,
                experimentCase.requiredEdges,
                seed,
                method,
                originalVertices,
                originalEdges,
                originalRequiredEdges,
                reducedVertices,
                reducedEdges,
                reducedRequiredEdges,
                reducedNonRequiredEdges,
                components,
                xVariables,
                yVariables,
                totalVariables,
                balanceConstraints,
                connectivityConstraints,
                totalConstraints,
                reductionTimeSeconds,
                solveTimeSeconds,
                totalTimeSeconds,
                objectiveValue,
                status,
                edgeReductionPercent,
                vertexReductionPercent
        );

        writeRawRow(csv, row);

        System.out.println(method
                + ": vertices=" + reducedVertices
                + ", edges=" + reducedEdges
                + ", variables=" + totalVariables
                + ", components=" + components
                + ", reductionTimeSeconds=" + format(reductionTimeSeconds)
                + ", solveTimeSeconds=" + format(solveTimeSeconds)
                + ", objective=" + format(objectiveValue)
                + ", status=" + status);

        return row;
    }

    private static void writeRawRow(CsvLogger csv, ExperimentRow row) {
        csv.writeLine(
                row.series,
                row.vertices,
                row.edges,
                row.requiredEdges,
                row.seed,
                row.method,
                row.originalVertices,
                row.originalEdges,
                row.originalRequiredEdges,
                row.reducedVertices,
                row.reducedEdges,
                row.reducedRequiredEdges,
                row.reducedNonRequiredEdges,
                row.components,
                row.xVariables,
                row.yVariables,
                row.totalVariables,
                row.balanceConstraints,
                row.connectivityConstraints,
                row.totalConstraints,
                format(row.reductionTimeSeconds),
                format(row.solveTimeSeconds),
                format(row.totalTimeSeconds),
                format(row.objectiveValue),
                row.status,
                format(row.edgeReductionPercent),
                format(row.vertexReductionPercent)
        );
    }


    private static long countConnectivityConstraints(int components) {
        if (components <= 1) {
            return 0;
        }

        if (components >= 62) {
            return -1;
        }

        return 2L * ((1L << components) - 2);
    }

    private static double percentReduction(int original, int reduced) {
        if (original == 0) {
            return 0.0;
        }

        return 100.0 * (original - reduced) / original;
    }

    private static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }
        return String.format(Locale.US, "%.4f", value);
    }

    private record ExperimentCase(int vertices, int edges, int requiredEdges) {
    }

    private record ExperimentRow(
            String series,
            int vertices,
            int edges,
            int requiredEdges,
            int seed,
            String method,
            int originalVertices,
            int originalEdges,
            int originalRequiredEdges,
            int reducedVertices,
            int reducedEdges,
            int reducedRequiredEdges,
            int reducedNonRequiredEdges,
            int components,
            int xVariables,
            int yVariables,
            int totalVariables,
            long balanceConstraints,
            long connectivityConstraints,
            long totalConstraints,
            double reductionTimeSeconds,
            double solveTimeSeconds,
            double totalTimeSeconds,
            double objectiveValue,
            String status,
            double edgeReductionPercent,
            double vertexReductionPercent
    ) {
    }
}