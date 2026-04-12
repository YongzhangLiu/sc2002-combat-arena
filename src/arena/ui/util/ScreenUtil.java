package arena.ui.util;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.screen.Screen;
import arena.ui.sprite.AsciiSprite;
import arena.ui.sprite.SpriteCatalog;

import java.io.IOException;

public final class ScreenUtil {
    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(120, 42);

    private ScreenUtil() {
    }

    public static TerminalSize dialogSizeForScreen(Screen screen, boolean fullScreen) {
        TerminalSize terminalSize = screen.getTerminalSize();
        TerminalSize preferred = fullScreen ? terminalSize : WINDOWED_SIZE;
        TerminalSize bounded = fitToTerminal(terminalSize, preferred);
        int dialogColumns = Math.max(20, bounded.getColumns());
        int dialogRows = Math.max(10, bounded.getRows());
        return new TerminalSize(dialogColumns, dialogRows);
    }

    private static TerminalSize fitToTerminal(TerminalSize terminalSize, TerminalSize preferred) {
        int availableColumns = Math.max(20, terminalSize.getColumns() - 2);
        int availableRows = Math.max(10, terminalSize.getRows() - 2);
        int columns = Math.min(preferred.getColumns(), availableColumns);
        int rows = Math.min(preferred.getRows(), availableRows);
        return new TerminalSize(columns, rows);
    }

    public static void addSpriteLines(Panel panel, String category, String name, int maxColumns, int maxRows) {
        try {
            AsciiSprite sprite = SpriteCatalog.load(category, name, "normal");
            if (sprite.getWidth() > maxColumns || sprite.getHeight() > maxRows) {
                panel.addComponent(DialogComposer.centered(new Label("[" + name + "]")));
                return;
            }
            int rows = Math.min(maxRows, sprite.getHeight());
            for (int index = 0; index < rows; index++) {
                panel.addComponent(DialogComposer.centered(new Label(TextFormatUtil.fittedLine(sprite.getLines().get(index), maxColumns))));
            }
            for (int index = rows; index < maxRows; index++) {
                panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
            }
        } catch (IOException exception) {
            panel.addComponent(DialogComposer.centered(new Label("[" + name + "]")));
        }
    }

    public static void setMouseReporting(Screen screen, boolean enabled) {
        if (!(screen instanceof com.googlecode.lanterna.screen.TerminalScreen terminalScreen)) {
            return;
        }

        try {
            com.googlecode.lanterna.terminal.Terminal terminal = terminalScreen.getTerminal();
            if (enabled && terminal instanceof com.googlecode.lanterna.terminal.ExtendedTerminal extendedTerminal) {
                extendedTerminal.setMouseCaptureMode(com.googlecode.lanterna.terminal.MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
                return;
            }

            // Swing terminals reject raw control characters (such as ESC) in putString().
            String terminalClassName = terminal.getClass().getName();
            if (terminalClassName.contains(".swing.")) {
                return;
            }

            String sequence = enabled
                ? "\u001b[?1000h\u001b[?1002h\u001b[?1003h\u001b[?1006h\u001b[?1015h"
                : "\u001b[?1000l\u001b[?1002l\u001b[?1003l\u001b[?1006l\u001b[?1015l";
            terminal.putString(sequence);
            terminal.flush();
        } catch (IOException | IllegalArgumentException exception) {
            // Some backends do not support this sequence; fail open for keyboard navigation.
        }
    }
}
