package arena.ui.sprite;

import java.util.ArrayList;
import java.util.Collections;
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
    
    public AsciiSprite clipRight(int maxWidth, int maxRows) {
        if (lines == null || lines.isEmpty() || maxWidth <= 0 || maxRows <= 0) {
            return new AsciiSprite(Collections.emptyList());
        }

        int rowsToInclude = Math.min(maxRows, lines.size());
        List<String> clipped = new ArrayList<>(rowsToInclude);
        for (int row = 0; row < rowsToInclude; row++) {
            String line = lines.get(row);
            if (line.length() > maxWidth) {
                clipped.add(line.substring(0, maxWidth));
            } else {
                clipped.add(line);
            }
        }
        return new AsciiSprite(clipped);
    }
}
