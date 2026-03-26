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

import arena.ui.DialogComposer;
import arena.ui.GameSetup;
import static arena.ui.UiScreenSupport.addSpriteLines;
import static arena.ui.UiScreenSupport.fittedLine;
import static arena.ui.UiScreenSupport.dialogSizeForScreen;

public class ItemSelectionScreen {
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
        
        int mainContentRows = 20;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);
        
        String headerLine = fittedLine(DialogComposer.formatDialogHeader("ITEM SELECTION", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("Select your item", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Panel horizontalPanel = new Panel(new GridLayout(5));
        horizontalPanel.addComponent(buildItemCard("Potion", "potion", "Heal 100 HP", setup, dialogSize, result, window));
        horizontalPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        horizontalPanel.addComponent(buildItemCard("Power Stone", "power_stone", "Free skill cast", setup, dialogSize, result, window));
        horizontalPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        horizontalPanel.addComponent(buildItemCard("Smoke Bomb", "smoke_bomb", "Gain evasion", setup, dialogSize, result, window));
        panel.addComponent(DialogComposer.centered(horizontalPanel));

        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(DialogComposer.centered(new Button("Back", () -> {
            result[0] = -1;
            window.close();
        })));
        
        DialogComposer.addVerticalPaddingBottom(panel, dialogSize.getRows(), mainContentRows);

        window.setComponent(panel);
        gui.addWindowAndWait(window);
        return result[0];
    }

    private static Panel buildItemCard(String itemName, String spriteName, String description, GameSetup setup, TerminalSize dialogSize, int[] result, BasicWindow window) {
        int cardWidth = Math.max(12, (dialogSize.getColumns() - 10) / 3);
        Panel card = new Panel(new LinearLayout());

        card.addComponent(DialogComposer.centered(new Label(fittedLine(itemName, cardWidth))));
        addSpriteLines(card, "item", spriteName, cardWidth, 6);
        card.addComponent(DialogComposer.centered(new Label(fittedLine(description, cardWidth))));
        card.addComponent(DialogComposer.centered(new Button("Pick", () -> {
            setup.item = itemName;
            result[0] = 1;
            window.close();
        })));
        return card;
    }
}
