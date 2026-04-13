package arena.ui.screen;

import java.util.Arrays;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TerminalSize;

import arena.ui.util.DialogComposer;
import arena.ui.GameSetup;
import static arena.ui.util.TextFormatUtil.fittedLine;
import static arena.ui.util.TextFormatUtil.fittedLines;
import static arena.ui.util.ScreenUtil.dialogSizeForScreen;

public class DifficultySelectionScreen {
    public static int open(
        Screen screen,
        MultiWindowTextGUI gui,
        boolean fullScreen,
        boolean asciiMode,
        GameSetup setup
    ) {
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        TerminalSize dialogSize = dialogSizeForScreen(screen, fullScreen);
        window.setFixedSize(dialogSize);

        int contentWidth = Math.max(8, dialogSize.getColumns() - 4);
        String[] infoLines = fittedLines(
            "Choose level:\n"
                + "Easy: 3 enemies (3 Goblins)\n"
                + "Medium: 4 enemies total (1 Goblin + 1 Wolf, then 2 Wolves)\n"
                + "Hard: 5 enemies total (2 Goblins, then 1 Goblin + 2 Wolves)\n",
            Math.max(1, dialogSize.getRows() - 14),
            contentWidth
        );

        final int[] result = {0};
        Panel panel = new Panel(new LinearLayout());
        
        int mainContentRows = 20;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);
        
        String headerLine = fittedLine(DialogComposer.formatDialogHeader("DIFFICULTY", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("Select difficulty", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        for (String line : infoLines) {
            panel.addComponent(DialogComposer.centered(new Label(line)));
        }
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(DialogComposer.centered(new Button("Easy", () -> {
            setup.difficulty = "Easy";
            result[0] = 1;
            window.close();
        })));
        panel.addComponent(DialogComposer.centered(new Button("Medium", () -> {
            setup.difficulty = "Medium";
            result[0] = 1;
            window.close();
        })));
        panel.addComponent(DialogComposer.centered(new Button("Hard", () -> {
            setup.difficulty = "Hard";
            result[0] = 1;
            window.close();
        })));
        panel.addComponent(DialogComposer.centered(new Button("Back", () -> {
            result[0] = -1;
            window.close();
        })));
        
        DialogComposer.addVerticalPaddingBottom(panel, dialogSize.getRows(), mainContentRows);

        window.setComponent(panel);
        gui.addWindowAndWait(window);
        return result[0];
    }
}
