package arena.ui;

import java.util.List;

public final class AsciiSprite {
    private final List<String> lines;
    private final int width;
    private final int height;

    public AsciiSprite(List<String> lines) {
        this.lines = lines;
        this.height = lines.size();
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, line.length());
        }
        this.width = maxWidth;
    }

    public List<String> getLines() {
        return lines;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean fits(int maxWidth, int maxHeight) {
        return width <= maxWidth && height <= maxHeight;
    }

    public String toMultilineText() {
        return String.join("\n", lines);
    }
}
