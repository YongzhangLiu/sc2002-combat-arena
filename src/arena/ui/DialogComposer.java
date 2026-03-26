package arena.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

public final class DialogComposer {
    private DialogComposer() {
    }

    public static <T extends Component> T centered(T component) {
        component.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        return component;
    }

    public static void addVerticalPaddingTop(Panel panel, int viewportRows, int contentRows) {
        int topRows = Math.max(0, (viewportRows - contentRows) / 2);
        if (topRows > 0) {
            panel.addComponent(new EmptySpace(new TerminalSize(1, topRows)));
        }
    }

    public static void addVerticalPaddingBottom(Panel panel, int viewportRows, int contentRows) {
        int topRows = Math.max(0, (viewportRows - contentRows) / 2);
        int bottomRows = Math.max(0, viewportRows - contentRows - topRows);
        if (bottomRows > 0) {
            panel.addComponent(new EmptySpace(new TerminalSize(1, bottomRows)));
        }
    }

    public static String formatDialogHeader(String title, boolean asciiMode) {
        final int innerWidth = 29;
        String normalized = title == null ? "" : title.trim();
        if (normalized.length() > innerWidth) {
            normalized = normalized.substring(0, innerWidth);
        }
        int totalPadding = innerWidth - normalized.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        String horizontal = asciiMode ? "-" : "─";
        String topLeft = asciiMode ? "+" : "╭";
        String topRight = asciiMode ? "+" : "╮";
        return topLeft + horizontal.repeat(leftPadding) + " " + normalized + " " + horizontal.repeat(rightPadding) + topRight;
    }

    public static String formatTopBorder(int width, boolean asciiMode) {
        String horizontal = asciiMode ? "-" : "─";
        String topLeft = asciiMode ? "+" : "╭";
        String topRight = asciiMode ? "+" : "╮";
        return topLeft + horizontal.repeat(width) + topRight;
    }

    public static String formatMiddleBorder(String text, int width, boolean asciiMode) {
        String vertical = asciiMode ? "|" : "│";
        String normalized = text == null ? "" : text.trim();
        if (normalized.length() > width) {
            normalized = normalized.substring(0, width);
        }
        int totalPadding = width - normalized.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return vertical + " ".repeat(leftPadding) + normalized + " ".repeat(rightPadding) + vertical;
    }

    public static String formatBottomBorder(int width, boolean asciiMode) {
        String horizontal = asciiMode ? "-" : "─";
        String bottomLeft = asciiMode ? "+" : "╰";
        String bottomRight = asciiMode ? "+" : "╯";
        return bottomLeft + horizontal.repeat(width) + bottomRight;
    }
}