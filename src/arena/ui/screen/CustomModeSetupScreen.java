package arena.ui.screen;

import java.util.Arrays;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;

import arena.ui.GameSetup;
import arena.ui.util.DialogComposer;
import arena.ui.util.ResizeHandler;
import static arena.ui.util.ScreenUtil.dialogSizeForScreen;
import static arena.ui.util.TextFormatUtil.fittedLine;
import static arena.ui.util.TextFormatUtil.fittedLines;

/**
 * After choosing Custom difficulty: pick opponent type and whether the attack QTE runs.
 */
public final class CustomModeSetupScreen {
    private CustomModeSetupScreen() {}

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

        final int[] result = {0};
        
        Panel panel = buildSelectionPanel(asciiMode, dialogSize, setup, result, window);
        window.setComponent(panel);

        ResizeHandler resizeHandler = ResizeHandler.attach(screen, gui, newSize -> {
            TerminalSize newSizeDialog = dialogSizeForScreen(screen, fullScreen);
            window.setFixedSize(newSizeDialog);
            Panel newPanel = buildSelectionPanel(asciiMode, newSizeDialog, setup, result, window);
            window.setComponent(newPanel);
        });

        gui.addWindowAndWait(window);
        resizeHandler.detach();
        return result[0];
    }

    private static Panel buildSelectionPanel(boolean asciiMode, TerminalSize dialogSize, GameSetup setup, int[] result, BasicWindow window) {
        int contentWidth = Math.max(8, dialogSize.getColumns() - 4);
        String[] infoLines = fittedLines(
            "Pick your opponent for 1v1 custom duel.\n"
                + "Warrior/Wizard opponents use items and full AI; Goblin/Wolf use standard enemy AI.\n"
                + "QTE: timing bar for Basic Attack only (when enabled).\n",
            Math.max(1, dialogSize.getRows() - 22),
            contentWidth
        );

        Panel panel = new Panel(new LinearLayout());
        int mainContentRows = 24;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);

        String headerLine = fittedLine(DialogComposer.formatDialogHeader("CUSTOM DUEL", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("Opponent & QTE", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        for (String line : infoLines) {
            panel.addComponent(DialogComposer.centered(new Label(line)));
        }
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Label summary = new Label("");
        Runnable refreshSummary = () -> {
            String opp = setup.customOpponentType != null ? setup.customOpponentType : "Random";
            String qte = setup.customQteEnabled ? "On" : "Off";
            summary.setText(fittedLine("Opponent: " + opp + "   |   QTE: " + qte, contentWidth));
        };
        refreshSummary.run();
        panel.addComponent(DialogComposer.centered(summary));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        panel.addComponent(DialogComposer.centered(new Label(fittedLine("Opponent type", contentWidth))));
        panel.addComponent(DialogComposer.centered(opponentButton("Warrior", setup, refreshSummary)));
        panel.addComponent(DialogComposer.centered(opponentButton("Wizard", setup, refreshSummary)));
        panel.addComponent(DialogComposer.centered(opponentButton("Goblin", setup, refreshSummary)));
        panel.addComponent(DialogComposer.centered(opponentButton("Wolf", setup, refreshSummary)));
        panel.addComponent(DialogComposer.centered(opponentButton("Random", setup, refreshSummary)));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        panel.addComponent(DialogComposer.centered(new Button("QTE: On", () -> {
            setup.customQteEnabled = true;
            refreshSummary.run();
        })));
        panel.addComponent(DialogComposer.centered(new Button("QTE: Off", () -> {
            setup.customQteEnabled = false;
            refreshSummary.run();
        })));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        panel.addComponent(DialogComposer.centered(new Button("Confirm", () -> {
            result[0] = 1;
            window.close();
        })));
        panel.addComponent(DialogComposer.centered(new Button("Back", () -> {
            result[0] = -1;
            window.close();
        })));

        DialogComposer.addVerticalPaddingBottom(panel, dialogSize.getRows(), mainContentRows);
        return panel;
    }

    private static Button opponentButton(String type, GameSetup setup, Runnable refreshSummary) {
        return new Button(type, () -> {
            setup.customOpponentType = type;
            refreshSummary.run();
        });
    }
}
