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
import arena.model.combatant.Goblin;
import arena.model.combatant.Wolf;
import arena.ui.DialogComposer;
import static arena.ui.UiScreenSupport.addSpriteLines;
import static arena.ui.UiScreenSupport.combatantStatBlock;
import static arena.ui.UiScreenSupport.fittedLine;
import static arena.ui.UiScreenSupport.fittedLines;
import static arena.ui.UiScreenSupport.dialogSizeForScreen;

public class EnemyInformationScreen {
    private static final int ENEMY_SPRITE_ROWS = 6;
    private static final int MIN_ENEMY_CARD_WIDTH = 18;

    public static int open(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode) {
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        TerminalSize dialogSize = dialogSizeForScreen(screen, fullScreen);
        window.setFixedSize(dialogSize);

        int contentWidth = Math.max(8, dialogSize.getColumns() - 4);
        final int[] result = {0};

        Panel panel = new Panel(new LinearLayout());
        
        int mainContentRows = 25;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);
        
        String headerLine = fittedLine(DialogComposer.formatDialogHeader("ENEMY INFORMATION", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("View your opponents", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Panel horizontalPanel = new Panel(new GridLayout(2));
        horizontalPanel.addComponent(buildEnemyCard("Goblin", "goblin", dialogSize));
        horizontalPanel.addComponent(buildEnemyCard("Wolf", "wolf", dialogSize));
        panel.addComponent(DialogComposer.centered(horizontalPanel));

        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(DialogComposer.centered(new Button("Continue to Difficulty", () -> {
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

    private static Panel buildEnemyCard(String enemyName, String spriteName, TerminalSize dialogSize) {
        int cardWidth = Math.max(MIN_ENEMY_CARD_WIDTH, (dialogSize.getColumns() - 10) / 2);
        Panel card = new Panel(new LinearLayout());

        card.addComponent(DialogComposer.centered(new Label(fittedLine(enemyName, cardWidth))));
        addSpriteLines(card, "enemy", spriteName, cardWidth, ENEMY_SPRITE_ROWS);

        Combatant enemy = "Goblin".equals(enemyName)
            ? new Goblin(enemyName)
            : new Wolf(enemyName);
        card.addComponent(DialogComposer.centered(new Label(DialogComposer.formatTopBorder(Math.max(1, cardWidth - 2), false))));
        String[] statLines = fittedLines(combatantStatBlock(enemy), 4, cardWidth);
        for (String statLine : statLines) {
            card.addComponent(DialogComposer.centered(new Label(fittedLine(statLine, cardWidth))));
        }
        card.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(Math.max(1, cardWidth - 2), false))));
        return card;
    }
}
