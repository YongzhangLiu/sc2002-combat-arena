package arena.ui.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility to crop sprite text to a fixed width by erasing rightmost pixels.
 */
public final class EnemySpriteClipper {
    private EnemySpriteClipper() {
    }

    public static List<String> clipRight(List<String> spriteLines, int maxWidth, int maxRows) {
        if (spriteLines == null || spriteLines.isEmpty() || maxWidth <= 0 || maxRows <= 0) {
            return Collections.emptyList();
        }

        int rows = Math.min(maxRows, spriteLines.size());
        List<String> clipped = new ArrayList<>(rows);
        for (int row = 0; row < rows; row++) {
            String line = spriteLines.get(row);
            if (line.length() > maxWidth) {
                clipped.add(line.substring(0, maxWidth));
            } else {
                clipped.add(line);
            }
        }
        return clipped;
    }
}
