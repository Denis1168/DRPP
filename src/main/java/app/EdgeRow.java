package app;

public record EdgeRow(int from, int to, int cost, boolean required, int r, int variableValue, int totalPasses) {
}