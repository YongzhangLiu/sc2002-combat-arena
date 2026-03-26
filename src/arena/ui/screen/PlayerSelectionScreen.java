package arena.ui.screen;

import java.util.Arrays;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TerminalSize;

import arena.model.combatant.Combatant;
import arena.model.combatant.Warrior;
import arena.model.combatant.Wizard;
import arena.ui.DialogComposer;
import arena.ui.GameSetup;
import static arena.ui.UiScreenSupport.addSpriteLines;
import static arena.ui.UiScreenSupport.combatantStatBlock;
import static arena.ui.UiScreenSupport.fittedLine;
import static arena.ui.UiScreenSupport.fittedLines;
import static arena.ui.UiScreenSupport.dialogSizeForScreen;

public class PlayerSelectionScreen {
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

        final int[] result = {0};
        Panel panel = new Panel(new LinearLayout());
        
        int mainContentRows = 24;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);
        
        String headerLine = fittedLine(DialogComposer.formatDialogHeader("PLAYER SETUP", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("Select your player class", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Panel horizontalPanel = new Panel(new GridLayout(2));
        horizontalPanel.addComponent(buildPlayerCard("Warrior", setup, dialogSize, result, window));
        horizontalPanel.addComponent(buildPlayerCard("Wizard", setup, dialogSize, result, window));
        panel.addComponent(DialogComposer.centered(horizontalPanel));

        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(DialogComposer.centered(new Button("Back", () -> {
            result[0] = 0;
            window.close();
        })));
        
        DialogComposer.addVerticalPaddingBottom(panel, dialogSize.getRows(), mainContentRows);

        window.setComponent(panel);
        gui.addWindowAndWait(window);
        return result[0];
    }

    private static Panel buildPlayerCard(String className, GameSetup setup, TerminalSize dialogSize, int[] result, BasicWindow window) {
        int cardWidth = Math.max(16, (dialogSize.getColumns() - 10) / 2);
        Panel card = new Panel(new LinearLayout());

        card.addComponent(DialogComposer.centered(new Label(fittedLine(className, cardWidth))));
        addSpriteLines(card, "player", className.toLowerCase(), cardWidth, 10);

        Combatant combatant = "Warrior".equals(className)
            ? new Warrior("Warrior")
            : new Wizard("Wizard");
        card.addComponent(DialogComposer.centered(new Label(DialogComposer.formatTopBorder(Math.max(1, cardWidth - 2), false))));
        String[] statLines = fittedLines(combatantStatBlock(combatant), 4, cardWidth);
        for (String statLine : statLines) {
            card.addComponent(DialogComposer.centered(new Label(fittedLine(statLine, cardWidth))));
        }
        card.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(Math.max(1, cardWidth - 2), false))));
        card.addComponent(DialogComposer.centered(new Button("Pick " + className, () -> {
            setup.playerClass = className;
            result[0] = 1;
            window.close();
        })));
        return card;
    }
}
