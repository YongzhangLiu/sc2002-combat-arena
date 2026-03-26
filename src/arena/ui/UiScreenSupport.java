package arena.ui;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.screen.Screen;

import arena.model.combatant.Combatant;

public final class UiScreenSupport {
    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(100, 42);

    private UiScreenSupport() {
    }

    public static String fittedLine(String line, int maxColumns) {
        if (line.length() <= maxColumns) {
            return line;
        }
        if (maxColumns <= 3) {
            return ".".repeat(Math.max(1, maxColumns));
        }
        return line.substring(0, maxColumns - 3) + "...";
    }

    public static String[] fittedLines(String text, int maxRows, int maxColumns) {
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

    public static TerminalSize dialogSizeForScreen(Screen screen, boolean fullScreen) {
        TerminalSize terminalSize = screen.getTerminalSize();
        TerminalSize preferred = fullScreen ? terminalSize : WINDOWED_SIZE;
        TerminalSize bounded = fitToTerminal(terminalSize, preferred);
        int dialogColumns = Math.max(20, bounded.getColumns());
        int dialogRows = Math.max(10, bounded.getRows());
        return new TerminalSize(dialogColumns, dialogRows);
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
                panel.addComponent(DialogComposer.centered(new Label(fittedLine(sprite.getLines().get(index), maxColumns))));
            }
            for (int index = rows; index < maxRows; index++) {
                panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
            }
        } catch (IOException exception) {
            panel.addComponent(DialogComposer.centered(new Label("[" + name + "]")));
        }
    }

    public static String combatantStatBlock(Combatant combatant) {
        return "HP: " + combatant.getMaxHp() + "\n"
            + "ATK: " + combatant.getAttack() + "\n"
            + "DEF: " + combatant.getBaseDefense() + "\n"
            + "SPD: " + combatant.getSpeed();
    }

    private static TerminalSize fitToTerminal(TerminalSize terminalSize, TerminalSize preferred) {
        int availableColumns = Math.max(20, terminalSize.getColumns() - 2);
        int availableRows = Math.max(10, terminalSize.getRows() - 2);
        int columns = Math.min(preferred.getColumns(), availableColumns);
        int rows = Math.min(preferred.getRows(), availableRows);
        return new TerminalSize(columns, rows);
    }
}
