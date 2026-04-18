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

import arena.ui.util.DialogComposer;
import arena.ui.util.ResizeHandler;
import arena.ui.GameSetup;
import static arena.ui.util.ScreenUtil.addSpriteLines;
import static arena.ui.util.TextFormatUtil.fittedLine;
import static arena.ui.util.ScreenUtil.dialogSizeForScreen;

public class ItemSelectionScreen {
    private static final String NONE = "None";

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
        final int[] activeSlot = {0};
        final String[] selectedItems = {
            normalizeItem(setup.itemSlot1),
            normalizeItem(setup.itemSlot2)
        };

        Panel panel = buildSelectionPanel(asciiMode, dialogSize, selectedItems, activeSlot, result, setup, window);
        window.setComponent(panel);

        ResizeHandler resizeHandler = ResizeHandler.attach(screen, gui, newSize -> {
            TerminalSize newSizeDialog = dialogSizeForScreen(screen, fullScreen);
            window.setFixedSize(newSizeDialog);
            Panel newPanel = buildSelectionPanel(asciiMode, newSizeDialog, selectedItems, activeSlot, result, setup, window);
            window.setComponent(newPanel);
        });

        gui.addWindowAndWait(window);
        resizeHandler.detach();
        return result[0];
    }

    private static Panel buildSelectionPanel(boolean asciiMode, TerminalSize dialogSize, String[] selectedItems, int[] activeSlot, int[] result, GameSetup setup, BasicWindow window) {
        int contentWidth = Math.max(8, dialogSize.getColumns() - 4);

        Panel panel = new Panel(new LinearLayout());
        
        int mainContentRows = 23;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);
        
        String headerLine = fittedLine(DialogComposer.formatDialogHeader("ITEM SELECTION", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("Pick up to 2 items", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        int rightPanelWidth = Math.max(24, dialogSize.getColumns() / 4);
        int cardsWidth = Math.max(30, dialogSize.getColumns() - rightPanelWidth - 8);
        int cardWidth = Math.max(12, (cardsWidth - 4) / 3);

        Label activeSlotLabel = new Label("");
        Label item1Label = new Label("");
        Label item2Label = new Label("");
        Runnable refreshSelections = () -> {
            activeSlotLabel.setText("Editing: Item " + (activeSlot[0] + 1));
            item1Label.setText("Item 1: " + selectedItems[0]);
            item2Label.setText("Item 2: " + selectedItems[1]);
        };
        refreshSelections.run();

        Panel bodyRow = new Panel(new GridLayout(3));
        bodyRow.addComponent(buildItemCard("Potion", "potion", "Heal 100 HP", cardWidth, selectedItems, activeSlot, refreshSelections));
        bodyRow.addComponent(buildItemCard("Power Stone", "power_stone", "Free skill cast", cardWidth, selectedItems, activeSlot, refreshSelections));
        bodyRow.addComponent(buildItemCard("Smoke Bomb", "smoke_bomb", "Gain evasion", cardWidth, selectedItems, activeSlot, refreshSelections));
        panel.addComponent(DialogComposer.centered(bodyRow));

        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Panel selectionPanel = new Panel(new LinearLayout());
        selectionPanel.addComponent(DialogComposer.centered(activeSlotLabel));
        selectionPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        selectionPanel.addComponent(DialogComposer.centered(item1Label));
        selectionPanel.addComponent(DialogComposer.centered(item2Label));
        selectionPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        selectionPanel.addComponent(DialogComposer.centered(new Button("Edit Item 1", () -> {
            activeSlot[0] = 0;
            refreshSelections.run();
        })));
        selectionPanel.addComponent(DialogComposer.centered(new Button("Edit Item 2", () -> {
            activeSlot[0] = 1;
            refreshSelections.run();
        })));
        selectionPanel.addComponent(DialogComposer.centered(new Button("Clear Active Slot", () -> {
            selectedItems[activeSlot[0]] = NONE;
            refreshSelections.run();
        })));
        panel.addComponent(DialogComposer.centered(selectionPanel));

        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(DialogComposer.centered(new Button("Confirm", () -> {
            setup.itemSlot1 = selectedItems[0];
            setup.itemSlot2 = selectedItems[1];
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

    private static Panel buildItemCard(String itemName, String spriteName, String description, int cardWidth, String[] selectedItems, int[] activeSlot, Runnable refreshSelections) {
        Panel card = new Panel(new LinearLayout());

        card.addComponent(DialogComposer.centered(new Label(fittedLine(itemName, cardWidth))));
        addSpriteLines(card, "item", spriteName, cardWidth, 6);
        card.addComponent(DialogComposer.centered(new Label(fittedLine(description, cardWidth))));
        card.addComponent(DialogComposer.centered(new Button("Pick", () -> {
            selectedItems[activeSlot[0]] = itemName;
            int otherSlot = 1 - activeSlot[0];
            if (NONE.equalsIgnoreCase(selectedItems[otherSlot])) {
                activeSlot[0] = otherSlot;
            }
            refreshSelections.run();
        })));
        return card;
    }

    private static String normalizeItem(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return NONE;
        }
        return itemName;
    }
}
