package ch.nostromo.flightsimlog.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvContent {

    List<String> columns;
    List<List<String>> values;

    public CsvContent(Path path, int expectedCols) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String firstLine = br.readLine();
            if (firstLine == null)
                throw new IOException("empty file");
            columns = Arrays.asList(firstLine.split(",", expectedCols));

            values = parseCSV(br);
        }
    }
    public static List<List<String>> parseCSV(BufferedReader br) throws IOException {
        List<List<String>> allLines = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {
            List<String> parsedLine = parseCSVLine(line);
            allLines.add(parsedLine);
        }

        return allLines;
    }

    private static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes; // toggle inQuotes
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(stripQuotes(currentField.toString()));
                currentField.setLength(0); // clear the current field
            } else {
                currentField.append(c); // add character to the current field
            }
        }
        // Add the last field
        result.add(stripQuotes(currentField.toString()));

        return result;
    }

    private static String stripQuotes(String field) {
        // Remove leading and trailing quotes
        if (field.startsWith("\"") && field.endsWith("\"")) {
            return field.substring(1, field.length() - 1);
        }
        return field;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getValues() {
        return values;
    }

}
