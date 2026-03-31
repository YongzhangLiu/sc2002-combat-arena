package arena.ui.util;

public final class TextFormatUtil {
    private TextFormatUtil() {
    }

    public static String fittedLine(String line, int maxColumns) {
        if (line == null) {
            return "";
        }
        if (line.length() <= maxColumns) {
            return line;
        }
        if (maxColumns <= 3) {
            return ".".repeat(Math.max(1, maxColumns));
        }
        return line.substring(0, maxColumns - 3) + "...";
    }

    public static String[] fittedLines(String text, int maxRows, int maxColumns) {
        if (text == null) {
            return new String[0];
        }
        String[] rawLines = text.split("\\n", -1);
        int rows = Math.min(rawLines.length, Math.max(1, maxRows));
        String[] output = new String[rows];
        for (int i = 0; i < rows; i++) {
            output[i] = fittedLine(rawLines[i], maxColumns);
        }
        if (rawLines.length > rows) {
            output[rows - 1] = fittedLine("...", maxColumns);
        }
        return output;
    }
}
