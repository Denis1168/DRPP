import graph.Graph;
import graph.GraphGenerator;
import reducers.ChristofidesReducer;
import reducers.EiseltReducer;
import solver.Solver;
import solver.SolverResult;
import visualization.DotExporter;

public class Main {
    public static void main(String[] args) {
        GraphGenerator generator = new GraphGenerator(18);

        Graph graph = generator.generate(
                10,   // количество вершин
                25,   // количество дуг
                8,    // количество обязательных дуг
                1,    // минимальный вес
                30   // максимальный вес
        );

//        GraphGenerator generator = new GraphGenerator(598465965);
//        Graph graph = generator.generate(
//                8,   // количество вершин
//                18,   // количество дуг
//                5,    // количество обязательных дуг
//                1,    // минимальный вес
//                30   // максимальный вес
//        );

        Graph reducedGraph1 = ChristofidesReducer.reduce(graph.copy());
        Graph reducedGraph2 = EiseltReducer.reduce(graph.copy());

        System.out.println("Исходный граф:");
        System.out.println("Вершин: " + graph.vertexCount());
        System.out.println("Дуг: " + graph.edgeCount());
        System.out.println("Обязательных дуг: " + graph.getRequiredEdges().size());

        System.out.println("\nChristofides:");
        System.out.println("Вершин: " + reducedGraph1.vertexCount());
        System.out.println("Дуг: " + reducedGraph1.edgeCount());
        System.out.println("Обязательных дуг: " + reducedGraph1.getRequiredEdges().size());

        System.out.println("\nEiselt:");
        System.out.println("Вершин: " + reducedGraph2.vertexCount());
        System.out.println("Дуг: " + reducedGraph2.edgeCount());
        System.out.println("Обязательных дуг: " + reducedGraph2.getRequiredEdges().size());

//        DotExporter.saveToPngFile(graph, "graph.png");
//        DotExporter.saveToPngFile(reducedGraph1, "reducedGraph1.png");
//        DotExporter.saveToPngFile(reducedGraph2, "reducedGraph2.png");

//        DotExporter.saveToSvgFile(graph, "graph");
//        DotExporter.saveToSvgFile(reducedGraph1, "reducedGraph1");
//        DotExporter.saveToSvgFile(reducedGraph2, "reducedGraph2");

        warmUp();

        SolverResult res1 = Solver.solve(reducedGraph1);
        res1.show();

        SolverResult res2 = Solver.solve(reducedGraph2);
        res2.show();
    }

    private static void warmUp() {
        GraphGenerator generator = new GraphGenerator(999);
        Graph graph = generator.generate(6, 12, 4, 1, 10);
        Graph reduced = EiseltReducer.reduce(graph);
        Solver.solve(reduced);
    }
}
