package app;

import com.jom.DoubleMatrixND;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.Html;

import com.vaadin.flow.theme.aura.Aura;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import graph.algorithms.RequiredComponentsFinder;
import reducers.ChristofidesReducer;
import reducers.EiseltReducer;
import solver.Solver;
import solver.SolverResult;
import visualization.DotExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Route("")
@PageTitle("DRPP Solver")
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
public class MainView extends VerticalLayout {

    private Graph originalGraph;
    private Graph eiseltGraph;
    private Graph christofidesGraph;

    private boolean eiseltBuilt = false;
    private boolean christofidesBuilt = false;

    private double eiseltReductionTime = -1;
    private double christofidesReductionTime = -1;

    private final Tab generationTab = new Tab("Генерация");
    private final Tab eiseltTab = new Tab("Eiselt");
    private final Tab christofidesTab = new Tab("Christofides");
    private final Tab solveTab = new Tab("Решение");

    private final VerticalLayout content = new VerticalLayout();

    private final Div generationGraphContainer = createGraphContainer();
    private final Div originalForEiseltContainer = createGraphContainer();
    private final Div eiseltGraphContainer = createGraphContainer();
    private final Div originalForChristofidesContainer = createGraphContainer();
    private final Div christofidesGraphContainer = createGraphContainer();
    private final Div solutionGraphContainer = createGraphContainer();

    private final IntegerField verticesField = new IntegerField("Вершины");
    private final IntegerField edgesField = new IntegerField("Дуги");
    private final IntegerField requiredField = new IntegerField("Обязательные дуги");
    private final IntegerField minCostField = new IntegerField("Мин. стоимость");
    private final IntegerField maxCostField = new IntegerField("Макс. стоимость");
    private final Checkbox randomSeedCheckbox = new Checkbox("Случайный seed");
    private final IntegerField seedField = new IntegerField("Seed генератора");

    private final Button generateButton = new Button("Сгенерировать граф");

    private final Select<String> solveGraphChoice = new Select<>("Какой граф решать?");
    private final Button solveButton = new Button("Запустить алгоритм");
    private final TextArea solveResultArea = new TextArea("Результаты");
    private final Grid<EdgeRow> solutionGrid = createEdgeGrid();

    public MainView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        H2 title = new H2("DRPP Solver");
        title.getStyle()
                .set("margin", "16px")
                .set("font-size", "28px");

        Tabs tabs = new Tabs(
                generationTab,
                eiseltTab,
                christofidesTab,
                solveTab
        );

        tabs.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            Tab selected = event.getSelectedTab();

            if (selected == generationTab) {
                showGenerationTab();
            } else if (selected == eiseltTab) {
                showEiseltTab();
            } else if (selected == christofidesTab) {
                showChristofidesTab();
            } else if (selected == solveTab) {
                showSolveTab();
            }
        });

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(title, tabs, content);
        expand(content);

        warmUp();

        configureGenerationControls();
        configureSolveControls();

        showGenerationTab();
    }

    private Div createGraphContainer() {
        Div div = new Div();
        div.setWidthFull();
        div.setHeight("560px");
        div.getStyle()
                .set("overflow", "auto")
                .set("border", "1px solid #d0d7de")
                .set("border-radius", "12px")
                .set("background", "white")
                .set("padding", "10px");
        return div;
    }

    private void setGraphSvg(Div container, Graph graph) {
        container.removeAll();

        String svg = DotExporter.returnSvgFile(graph);

        Html svgComponent = new Html(
                "<div class='graph-svg-wrapper'>" + svg + "</div>"
        );

        container.add(svgComponent);
    }

    private void setSvgContent(Div container, String svg) {
        container.removeAll();

        Html svgComponent = new Html(
                "<div class='graph-svg-wrapper'>" + svg + "</div>"
        );

        container.add(svgComponent);
    }

    private void configureGenerationControls() {
        verticesField.setStepButtonsVisible(true);
        verticesField.setValue(10);
        verticesField.setMin(2);

        edgesField.setStepButtonsVisible(true);
        edgesField.setValue(25);
        edgesField.setMin(1);

        requiredField.setStepButtonsVisible(true);
        requiredField.setValue(8);
        requiredField.setMin(1);

        minCostField.setStepButtonsVisible(true);
        minCostField.setValue(1);
        minCostField.setMin(1);

        maxCostField.setStepButtonsVisible(true);
        maxCostField.setValue(30);
        maxCostField.setMin(1);

        randomSeedCheckbox.setValue(true);

        seedField.setValue(18);
        seedField.setEnabled(false);

        randomSeedCheckbox.addValueChangeListener(event -> {
            seedField.setEnabled(!event.getValue());
        });

        generateButton.setWidthFull();

        generateButton.addClickListener(event -> {
            try {
                int seed;

                if (randomSeedCheckbox.getValue()) {
                    seed = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
                    seedField.setValue(seed);
                } else {
                    seed = seedField.getValue();
                }

                GraphGenerator generator = new GraphGenerator(seed);

                originalGraph = generator.generate(
                        verticesField.getValue(),
                        edgesField.getValue(),
                        requiredField.getValue(),
                        minCostField.getValue(),
                        maxCostField.getValue()
                );

                eiseltGraph = null;
                christofidesGraph = null;

                eiseltBuilt = false;
                christofidesBuilt = false;

                eiseltReductionTime = -1;
                christofidesReductionTime = -1;

                setGraphSvg(generationGraphContainer, originalGraph);

                Notification.show("Граф сгенерирован");

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Ошибка: " + ex.getMessage());
            }
        });
    }

    private void showGenerationTab() {
        content.removeAll();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        VerticalLayout leftPanel = new VerticalLayout();
        leftPanel.setWidth("300px");
        leftPanel.setPadding(true);
        leftPanel.setSpacing(true);
        leftPanel.getStyle()
                .set("background", "#f8fafc")
                .set("border-right", "1px solid #e5e7eb");

        leftPanel.add(
                new H2("Исходный граф"),
                verticesField,
                edgesField,
                requiredField,
                minCostField,
                maxCostField,
                randomSeedCheckbox,
                seedField,
                generateButton
        );

        VerticalLayout rightPanel = new VerticalLayout();
        rightPanel.setSizeFull();
        rightPanel.setPadding(true);

        rightPanel.add(generationGraphContainer);

        layout.add(leftPanel, rightPanel);
        layout.setFlexGrow(1, rightPanel);

        content.add(layout);
    }

    private void showEiseltTab() {
        content.removeAll();

        if (originalGraph == null) {
            content.add(createEmptyMessage("Сначала нужно сгенерировать исходный граф."));
            return;
        }

        if (!eiseltBuilt) {
            buildEiseltReduction();
        }

        content.add(createCompareLayout(
                "Исходный граф",
                originalForEiseltContainer,
                "Граф после сокращения Eiselt",
                eiseltGraphContainer,
                getReductionInfo("Eiselt", eiseltGraph, eiseltReductionTime)
        ));
    }

    private void showChristofidesTab() {
        content.removeAll();

        if (originalGraph == null) {
            content.add(createEmptyMessage("Сначала нужно сгенерировать исходный граф."));
            return;
        }

        if (!christofidesBuilt) {
            buildChristofidesReduction();
        }

        content.add(createCompareLayout(
                "Исходный граф",
                originalForChristofidesContainer,
                "Граф после сокращения Christofides",
                christofidesGraphContainer,
                getReductionInfo("Christofides", christofidesGraph, christofidesReductionTime)
        ));
    }

    private void buildEiseltReduction() {
        long start = System.nanoTime();
        eiseltGraph = EiseltReducer.reduce(originalGraph.copy());
        long end = System.nanoTime();
        eiseltReductionTime = (end - start) / 1_000_000_000.0;
        eiseltBuilt = true;

        setGraphSvg(originalForEiseltContainer, originalGraph);
        setGraphSvg(eiseltGraphContainer, eiseltGraph);
    }

    private void buildChristofidesReduction() {
        long start = System.nanoTime();
        christofidesGraph = ChristofidesReducer.reduce(originalGraph.copy());
        long end = System.nanoTime();
        christofidesReductionTime = (end - start) / 1_000_000_000.0;
        christofidesBuilt = true;

        setGraphSvg(originalForChristofidesContainer, originalGraph);
        setGraphSvg(christofidesGraphContainer, christofidesGraph);
    }

    private static void warmUp() {
        GraphGenerator generator = new GraphGenerator(999);
        Graph graph = generator.generate(6, 12, 4, 1, 10);
        Graph reduced1 = EiseltReducer.reduce(graph);
        Graph reduced2 = ChristofidesReducer.reduce(graph);
        Solver.solve(reduced1);
        Solver.solve(reduced2);
    }

    private Component createCompareLayout(
            String leftTitle,
            Div leftContainer,
            String rightTitle,
            Div rightContainer,
            String info
    ) {
        VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        root.setPadding(true);

        HorizontalLayout images = new HorizontalLayout();
        images.setSizeFull();
        images.setSpacing(true);

        VerticalLayout left = new VerticalLayout(new H2(leftTitle), leftContainer);
        left.setSizeFull();

        VerticalLayout right = new VerticalLayout(new H2(rightTitle), rightContainer);
        right.setSizeFull();

        images.add(left, right);
        images.setFlexGrow(1, left, right);

        TextArea infoText = new TextArea("Информация");
        infoText.setValue(info);
        infoText.setReadOnly(true);
        infoText.setWidthFull();
        infoText.setHeightFull();

        root.add(infoText, images);
        root.expand(images);

        return root;
    }

    private void configureSolveControls() {
        solveGraphChoice.setItems("Original", "Eiselt", "Christofides");
        solveGraphChoice.setValue("Christofides");

        solveResultArea.setReadOnly(true);
        solveResultArea.setWidthFull();
        solveResultArea.setHeightFull();

        solutionGrid.setSizeFull();

        solveButton.addClickListener(event -> {
            try {
                Graph graphToSolve = getGraphByName(solveGraphChoice.getValue());

                if (graphToSolve == null) {
                    Notification notification = new Notification();
                    notification.addThemeVariants(NotificationVariant.ERROR);
                    notification.setDuration(5000);
                    HorizontalLayout layout = new HorizontalLayout(
                            new Text("Исходный граф ещё не построен"));
                    notification.add(layout);
                    notification.open();
                    return;
                }

                SolverResult result = Solver.solve(graphToSolve);
                double reductionTime = getReductionTimeByName(solveGraphChoice.getValue());

                int reducedVertices = graphToSolve.vertexCount();
                int reducedEdges = graphToSolve.edgeCount();
                int reducedRequiredEdges = graphToSolve.getRequiredEdges().size();
                int reducedNonRequiredEdges = reducedEdges - reducedRequiredEdges;
                int components = RequiredComponentsFinder.find(graphToSolve).size();

                solveResultArea.setValue(
                        "Граф: " + solveGraphChoice.getValue() + "\n" +
                                "Значение целевой функции: " + result.getOptimalCost() + "\n" +
                                "Время сокращения графа: " + format(reductionTime) + " с.\n" +
                                "Время решения модели: " + format(result.getModelSolveTime()) + " с.\n" +
                                "Общее время: " + format(reductionTime + result.getModelSolveTime()) + " с.\n" +
                                "Вершины: " + reducedVertices + "\n" +
                                "Дуги: " + reducedEdges + "\n" +
                                "Обязательные дуги: " + reducedRequiredEdges + "\n" +
                                "Количество компонент связности в графе из обязательных дуг: " + components + "\n" +
                                "Переменных: " + (reducedEdges + reducedRequiredEdges) + "\n" +
                                "Общее число ограничений: " + (reducedVertices + (2L * ((1L << components) - 2)))
                );

                solutionGrid.setItems(buildEdgeRows(graphToSolve, result));

                String solutionSvg = DotExporter.returnSolutionSvg(graphToSolve, result);
                setSvgContent(solutionGraphContainer, solutionSvg);

                Notification notification = new Notification();
                notification.addThemeVariants(NotificationVariant.SUCCESS);
                notification.setDuration(5000);
                HorizontalLayout layout = new HorizontalLayout(
                        new Text("Решение найдено"));
                notification.add(layout);
                notification.open();

            } catch (Exception ex) {
                ex.printStackTrace();
                solveResultArea.setValue("Ошибка:\n" + ex.getMessage());
                Notification.show("Ошибка при решении");
            }
        });
    }

    private void showSolveTab() {
        content.removeAll();

        VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        root.setPadding(true);
        root.setSpacing(true);

        HorizontalLayout top = new HorizontalLayout();
        top.setAlignItems(FlexComponent.Alignment.END);
        top.add(solveGraphChoice, solveButton);

        HorizontalLayout mid = new HorizontalLayout();
        mid.setSizeFull();
        mid.setAlignItems(FlexComponent.Alignment.END);
        mid.add(solveResultArea, solutionGrid);
        mid.setFlexGrow(1, solveResultArea);
        mid.setFlexGrow(3, solutionGrid);

        VerticalLayout graphBlock = new VerticalLayout();
        graphBlock.setWidthFull();
        graphBlock.setPadding(false);
        graphBlock.setSpacing(false);
        graphBlock.add(new H2("Граф решения"), solutionGraphContainer);

        root.add(top, mid, graphBlock);
        root.setFlexGrow(1, mid);
        root.setFlexGrow(1, graphBlock);

        content.add(root);
    }

    private String format(double number) {
        return String.format(Locale.US, "%.4f", number);
    }

    private Grid<EdgeRow> createEdgeGrid() {
        Grid<EdgeRow> grid = new Grid<>(EdgeRow.class, false);

        grid.addColumn(EdgeRow::from).setHeader("From").setAutoWidth(true);
        grid.addColumn(EdgeRow::to).setHeader("To").setAutoWidth(true);
        grid.addColumn(EdgeRow::cost).setHeader("Стоимость").setAutoWidth(true);
        grid.addColumn(row -> row.required() ? "Да" : "Нет").setHeader("Обязательная").setAutoWidth(true);
        grid.addColumn(EdgeRow::totalPasses).setHeader("Кол-во повторений").setAutoWidth(true);

        grid.setPartNameGenerator(row ->
                row.totalPasses() > 0 ? "used-edge-row" : "unused-edge-row"
        );

        return grid;
    }

    private List<EdgeRow> buildEdgeRows(Graph graph, SolverResult result) {
        List<EdgeRow> rows = new ArrayList<>();

        List<Edge> requiredEdges = new ArrayList<>(graph.getRequiredEdges());
        List<Edge> nonRequiredEdges = graph.getEdges()
                .stream()
                .filter(edge -> !edge.isRequired)
                .toList();

        DoubleMatrixND xSolution = result.getXSolution();
        DoubleMatrixND ySolution = result.getYSolution();

        for (int i = 0; i < requiredEdges.size(); i++) {
            Edge edge = requiredEdges.get(i);

            int x = (int) Math.round(xSolution.get(new int[] {0, i}));
            int total = 1 + edge.r + x;

            rows.add(new EdgeRow(
                    edge.from.id,
                    edge.to.id,
                    edge.cost,
                    true,
                    edge.r,
                    x,
                    total
            ));
        }

        for (int i = 0; i < nonRequiredEdges.size(); i++) {
            Edge edge = nonRequiredEdges.get(i);

            int y = (int) Math.round(ySolution.get(new int[] {0, i}));

            rows.add(new EdgeRow(
                    edge.from.id,
                    edge.to.id,
                    edge.cost,
                    false,
                    0,
                    y,
                    y
            ));
        }

        return rows;
    }

    private Graph getGraphByName(String name) {
        return switch (name) {
            case "Original" -> originalGraph;

            case "Eiselt" -> {
                if (originalGraph != null && !eiseltBuilt) {
                    buildEiseltReduction();
                }
                yield eiseltGraph;
            }

            case "Christofides" -> {
                if (originalGraph != null && !christofidesBuilt) {
                    buildChristofidesReduction();
                }
                yield christofidesGraph;
            }

            default -> null;
        };
    }

    private double getReductionTimeByName(String name) {
        return switch (name) {
            case "Eiselt" -> eiseltReductionTime;
            case "Christofides" -> christofidesReductionTime;
            default -> 0;
        };
    }

    private Component createEmptyMessage(String text) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.add(new Span(text));
        return layout;
    }

    private String getReductionInfo(String method, Graph graph, double time) {
        return "Сокращение " + method + " выполнено\n\n" +
                "Время сокращения: " + format(time) + " с.\n" +
                "Вершины исходного графа: " + originalGraph.vertexCount() + "\n" +
                "Дуги исходного графа: " + originalGraph.edgeCount() + "\n" +
                "Обязательные дуги исходного графа: " + originalGraph.getRequiredEdges().size() + "\n\n" +
                "Вершины после сокращения: " + graph.vertexCount() + "\n" +
                "Дуги после сокращения: " + graph.edgeCount() + "\n" +
                "Обязательные дуги после сокращения: " + graph.getRequiredEdges().size() + "\n" +
                "Необязательные дуги после сокращения: " +
                (graph.edgeCount() - graph.getRequiredEdges().size());
    }
}