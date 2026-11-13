import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvLogger implements AutoCloseable {
    private final BufferedWriter writer;

    public CsvLogger(String fileName, String... headers) {
        try {
            writer = Files.newBufferedWriter(
                    Path.of(fileName),
                    StandardCharsets.UTF_8
            );

            writeHeader(headers);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create CSV file", e);
        }
    }

    private void writeHeader(String... headers) {
        writeLine(headers);
    }

    public void writeLine(Object... values) {
        try {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    writer.write(",");
                }

                writer.write(escapeCsv(String.valueOf(values[i])));
            }

            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Cannot write CSV line", e);
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot close CSV file", e);
        }
    }
}