package arena.ui;

import arena.model.combatant.Combatant;
import arena.model.combatant.Goblin;
import arena.model.combatant.Warrior;
import arena.model.combatant.Wizard;
import arena.model.combatant.Wolf;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanternaStartMenuApp {
    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(100, 42);

    public static void main(String[] args) throws IOException {
        UiConfig config = new UiConfig(false, false);
        applyArgs(args, config);

        boolean keepRunning = true;
        while (keepRunning) {
            keepRunning = runSession(config);
        }
    }

    private static boolean runSession(UiConfig config) throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        if (!config.fullScreen) {
            terminalFactory.setInitialTerminalSize(WINDOWED_SIZE);
        }

        Screen screen = terminalFactory.createScreen();
        screen.startScreen();

        SessionResult result = new SessionResult(false, false);
        TerminalSize terminalSize = screen.getTerminalSize();
        TerminalSize activeWindowSize = config.fullScreen
            ? terminalSize
            : fitToTerminal(terminalSize, WINDOWED_SIZE);
        int viewportRows = activeWindowSize.getRows();
        int viewportColumns = activeWindowSize.getColumns();

        try {
            MultiWindowTextGUI gui = new MultiWindowTextGUI(
                screen,
                new DefaultWindowManager(),
                new EmptySpace(TextColor.ANSI.DEFAULT)
            );

            BasicWindow window = new BasicWindow();
            window.setHints(config.fullScreen
                ? Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.FULL_SCREEN, Window.Hint.EXPANDED)
                : Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
            if (!config.fullScreen) {
                window.setFixedSize(activeWindowSize);
            }

            Panel panel = new Panel(new LinearLayout());
            int mainContentRows = 12;
            DialogComposer.addVerticalPaddingTop(panel, viewportRows, mainContentRows);

            int mainBorderWidth = Math.max(8, Math.min(30, viewportColumns - 2));
            panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatTopBorder(mainBorderWidth, config.asciiMode))));
            panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("COMBAT  ARENA", mainBorderWidth, config.asciiMode))));
            panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(mainBorderWidth, config.asciiMode))));
            panel.addComponent(new EmptySpace(new TerminalSize(1, 2)));

            panel.addComponent(DialogComposer.centered(new Label("Display: " + (config.fullScreen ? "Fullscreen" : "Windowed"))));
            panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

            panel.addComponent(DialogComposer.centered(new Button("New Game", () -> {
                startNewGameFlow(screen, gui, config.fullScreen, config.asciiMode);
            })));
            panel.addComponent(DialogComposer.centered(new Button("View Controls", () -> {
                showMessage(screen, gui, config.fullScreen, config.asciiMode, "CONTROLS",
                        "- Up/Down: Navigate\n" +
                        "- Enter: Confirm\n" +
                        "- Esc: Back/Close");
            })));
            panel.addComponent(DialogComposer.centered(new Button("Sprite Demo", () -> {
                showSpriteDemo(screen, gui, config.fullScreen, config.asciiMode);
            })));
            panel.addComponent(DialogComposer.centered(new Button("Options", () -> {
                openOptions(screen, gui, config, result);
                if (result.restartRequested) {
                    window.close();
                }
            })));
            panel.addComponent(DialogComposer.centered(new Button("Exit", () -> {
                result.exitRequested = true;
                window.close();
            })));
            DialogComposer.addVerticalPaddingBottom(panel, viewportRows, mainContentRows);

            window.setComponent(panel);

            gui.addWindowAndWait(window);
        } finally {
            screen.stopScreen();
        }

        if (result.exitRequested) {
            return false;
        }
        return result.restartRequested;
    }

    private static void openOptions(Screen screen, MultiWindowTextGUI gui, UiConfig config, SessionResult result) {

        BasicWindow optionsWindow = new BasicWindow();
        optionsWindow.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        TerminalSize dialogSize = dialogSizeForScreen(screen, config.fullScreen);
        optionsWindow.setFixedSize(dialogSize);

        Panel optionsPanel = new Panel(new LinearLayout());
        int optionsContentRows = 10;
        int optionsBorderWidth = Math.max(8, Math.min(30, dialogSize.getColumns() - 2));
        DialogComposer.addVerticalPaddingTop(optionsPanel, dialogSize.getRows(), optionsContentRows);
        optionsPanel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatTopBorder(optionsBorderWidth, config.asciiMode))));
        optionsPanel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("OPTIONS", optionsBorderWidth, config.asciiMode))));
        optionsPanel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(optionsBorderWidth, config.asciiMode))));
        optionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 2)));

        final Button[] modeToggleButton = new Button[1];
        modeToggleButton[0] = new Button(toggleModeLabel(config.fullScreen), () -> {
            config.fullScreen = !config.fullScreen;
            modeToggleButton[0].setLabel(toggleModeLabel(config.fullScreen));
        });
        optionsPanel.addComponent(DialogComposer.centered(modeToggleButton[0]));
        optionsPanel.addComponent(DialogComposer.centered(new Label("(Apply restarts UI session)")));
        optionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        optionsPanel.addComponent(DialogComposer.centered(new Button("Apply and Return", () -> {
            result.restartRequested = true;
            optionsWindow.close();
        })));
        optionsPanel.addComponent(DialogComposer.centered(new Button("Back", optionsWindow::close)));
        DialogComposer.addVerticalPaddingBottom(optionsPanel, dialogSize.getRows(), optionsContentRows);

        optionsWindow.setComponent(optionsPanel);
        gui.addWindowAndWait(optionsWindow);
    }

    private static void showMessage(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode, String title, String text) {

        BasicWindow messageWindow = new BasicWindow();
        messageWindow.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        TerminalSize dialogSize = dialogSizeForScreen(screen, fullScreen);
        messageWindow.setFixedSize(dialogSize);

        Panel panel = new Panel(new LinearLayout());
        int contentWidth = Math.max(8, dialogSize.getColumns() - 4);
        String[] displayLines = fittedMessageLines(text, dialogSize, contentWidth);
        int messageLines = displayLines.length;
        int messageContentRows = messageLines + 5;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), messageContentRows);
        String headerLine = fittedLine(DialogComposer.formatDialogHeader(title, asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        for (String line : displayLines) {
            panel.addComponent(DialogComposer.centered(new Label(line)));
        }
        int messageBorderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(messageBorderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 2)));
        panel.addComponent(DialogComposer.centered(new Button("OK", messageWindow::close)));
        DialogComposer.addVerticalPaddingBottom(panel, dialogSize.getRows(), messageContentRows);

        messageWindow.setComponent(panel);
        gui.addWindowAndWait(messageWindow);
    }

    private static String[] fittedMessageLines(String text, TerminalSize dialogSize, int maxColumns) {
        String[] rawLines = text.split("\\n", -1);
        int maxTextRows = Math.max(1, dialogSize.getRows() - 5);

        int usedRows = Math.min(rawLines.length, maxTextRows);
        String[] fitted = new String[usedRows];
        for (int i = 0; i < usedRows; i++) {
            fitted[i] = fittedLine(rawLines[i], maxColumns);
        }

        if (rawLines.length > maxTextRows) {
            fitted[maxTextRows - 1] = fittedLine("...", maxColumns);
        }
        return fitted;
    }

    private static String fittedLine(String line, int maxColumns) {
        if (line.length() <= maxColumns) {
            return line;
        }
        if (maxColumns <= 3) {
            return ".".repeat(Math.max(1, maxColumns));
        }
        return line.substring(0, maxColumns - 3) + "...";
    }

    private static void showSpriteDemo(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode) {
        try {
            TerminalSize dialogSize = dialogSizeForScreen(screen, fullScreen);
            int maxSpriteWidth = Math.max(8, dialogSize.getColumns() - 12);
            int maxTextRows = Math.max(3, dialogSize.getRows() - 5);

            AsciiSprite arena = SpriteCatalog.loadArenaStrip("forest", maxSpriteWidth);
            AsciiSprite warrior = SpriteCatalog.load("player", "warrior", "normal");
            AsciiSprite wizard = SpriteCatalog.load("player", "wizard", "normal");
            AsciiSprite goblin = SpriteCatalog.load("enemy", "goblin", "normal");
            AsciiSprite wolf = SpriteCatalog.load("enemy", "wolf", "normal");

            String spriteText = buildSpriteDemoText(arena, warrior, wizard, goblin, wolf, maxSpriteWidth, maxTextRows);
            showMessage(screen, gui, fullScreen, asciiMode, "SPRITE DEMO", spriteText);
        } catch (IOException exception) {
            showMessage(screen, gui, fullScreen, asciiMode, "ERROR",
                "Failed to load sprite files from assets/sprites.\n" +
                    "Details: " + exception.getMessage());
        }
    }

    private static void startNewGameFlow(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode) {
        GameSetup setup = new GameSetup("Warrior", "Potion", "Easy");

        int currentScreen = 0; // 0: player, 1: item, 2: enemy, 3: difficulty
        while (currentScreen >= 0 && currentScreen < 4) {
            int result = currentScreen == 0 ? openPlayerSelection(screen, gui, fullScreen, asciiMode, setup)
                       : currentScreen == 1 ? openItemSelection(screen, gui, fullScreen, asciiMode, setup)
                       : currentScreen == 2 ? openEnemyInformation(screen, gui, fullScreen, asciiMode)
                       : openDifficultySelection(screen, gui, fullScreen, asciiMode, setup);
            
            if (result == -1) {
                currentScreen--;
            } else if (result == 1) {
                currentScreen++;
            } else {
                return;
            }
        }

        showMessage(screen, gui, fullScreen, asciiMode, "READY", buildSetupSummary(setup));
    }

    private static int openPlayerSelection(
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
        horizontalPanel.addComponent(buildPlayerChoiceCardWithAutoClose("Warrior", setup, dialogSize, result, window));
        horizontalPanel.addComponent(buildPlayerChoiceCardWithAutoClose("Wizard", setup, dialogSize, result, window));
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

    private static Panel buildPlayerChoiceCardWithAutoClose(String className, GameSetup setup, TerminalSize dialogSize, int[] result, BasicWindow window) {
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

    private static int openItemSelection(
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
        horizontalPanel.addComponent(buildItemChoiceCardWithAutoClose("Potion", "potion", setup, dialogSize, result, window));
        horizontalPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        horizontalPanel.addComponent(buildItemChoiceCardWithAutoClose("Power Stone", "power_stone", setup, dialogSize, result, window));
        horizontalPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        horizontalPanel.addComponent(buildItemChoiceCardWithAutoClose("Smoke Bomb", "smoke_bomb", setup, dialogSize, result, window));
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

    private static Panel buildItemChoiceCardWithAutoClose(String itemName, String spriteName, GameSetup setup, TerminalSize dialogSize, int[] result, BasicWindow window) {
        int cardWidth = Math.max(12, (dialogSize.getColumns() - 10) / 3);
        Panel card = new Panel(new LinearLayout());

        card.addComponent(DialogComposer.centered(new Label(fittedLine(itemName, cardWidth))));
        addSpriteLines(card, "item", spriteName, cardWidth, 6);
        card.addComponent(DialogComposer.centered(new Button("Pick", () -> {
            setup.item = itemName;
            result[0] = 1;
            window.close();
        })));
        return card;
    }

    private static Panel buildEnemyCardForInformation(String enemyName, String spriteName, TerminalSize dialogSize) {
        int cardWidth = Math.max(16, (dialogSize.getColumns() - 10) / 2);
        Panel card = new Panel(new LinearLayout());

        card.addComponent(DialogComposer.centered(new Label(fittedLine(enemyName, cardWidth))));
        addSpriteLines(card, "enemy", spriteName, cardWidth, 5);

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

    private static int openEnemyInformation(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode) {
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        TerminalSize dialogSize = dialogSizeForScreen(screen, fullScreen);
        window.setFixedSize(dialogSize);

        int contentWidth = Math.max(8, dialogSize.getColumns() - 4);
        final int[] result = {0};

        Panel panel = new Panel(new LinearLayout());
        
        int mainContentRows = 24;
        DialogComposer.addVerticalPaddingTop(panel, dialogSize.getRows(), mainContentRows);
        
        String headerLine = fittedLine(DialogComposer.formatDialogHeader("ENEMY INFORMATION", asciiMode), contentWidth);
        panel.addComponent(DialogComposer.centered(new Label(headerLine)));
        int borderWidth = Math.max(1, headerLine.length() - 2);
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatMiddleBorder("View your opponents", borderWidth, asciiMode))));
        panel.addComponent(DialogComposer.centered(new Label(DialogComposer.formatBottomBorder(borderWidth, asciiMode))));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        Panel horizontalPanel = new Panel(new GridLayout(2));
        horizontalPanel.addComponent(buildEnemyCardForInformation("Goblin", "goblin", dialogSize));
        horizontalPanel.addComponent(buildEnemyCardForInformation("Wolf", "wolf", dialogSize));
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

    private static int openDifficultySelection(
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
                + "Easy   - 2 enemies\n"
                + "Medium - 3 enemies\n"
                + "Hard   - 4 enemies",
            Math.max(3, dialogSize.getRows() - 14),
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

    private static String[] fittedLines(String text, int maxRows, int maxColumns) {
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

    private static String buildSetupSummary(GameSetup setup) {
        return "Player: " + setup.playerClass + "\n"
            + "Item: " + setup.item + "\n"
            + "Difficulty: " + setup.difficulty + "\n"
            + "\n"
            + "Next: connect setup to battle engine flow.";
    }

    private static String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }

    private static void addSpriteLines(Panel panel, String category, String name, int maxColumns, int maxRows) {
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

    private static String combatantStatBlock(Combatant combatant) {
        return "HP: " + combatant.getMaxHp() + "\n"
            + "ATK: " + combatant.getAttack() + "\n"
            + "DEF: " + combatant.getBaseDefense() + "\n"
            + "SPD: " + combatant.getSpeed();
    }

    private static String buildSpriteDemoText(
        AsciiSprite arena,
        AsciiSprite warrior,
        AsciiSprite wizard,
        AsciiSprite goblin,
        AsciiSprite wolf,
        int maxSpriteWidth,
        int maxTextRows
    ) {
        List<DemoSection> sections = new ArrayList<>();
        sections.add(new DemoSection("Arena", arena, "[Arena unavailable]", false));
        sections.add(new DemoSection("Warrior", warrior, "[Warrior model hidden: not enough space]", true));
        sections.add(new DemoSection("Wizard", wizard, "[Wizard model hidden: not enough space]", true));
        sections.add(new DemoSection("Goblin", goblin, "[Goblin model hidden: not enough space]", true));
        sections.add(new DemoSection("Wolf", wolf, "[Wolf model hidden: not enough space]", true));

        for (DemoSection section : sections) {
            if (section.sprite == null) {
                section.useFallback = true;
                continue;
            }
            if (section.checkWidth && section.sprite.getWidth() > maxSpriteWidth) {
                section.useFallback = true;
            }
        }

        while (estimateRows(sections) > maxTextRows) {
            int index = findLastVisibleSpriteSection(sections);
            if (index < 0) {
                break;
            }
            sections.get(index).useFallback = true;
        }

        StringBuilder output = new StringBuilder();
        for (int index = 0; index < sections.size(); index++) {
            DemoSection section = sections.get(index);
            output.append(section.title).append(":\n");
            if (section.useFallback || section.sprite == null) {
                output.append(section.fallbackText);
            } else {
                output.append(section.sprite.toMultilineText());
            }
            if (index < sections.size() - 1) {
                output.append("\n\n");
            }
        }
        return output.toString();
    }

    private static int estimateRows(List<DemoSection> sections) {
        int rows = 0;
        for (int index = 0; index < sections.size(); index++) {
            DemoSection section = sections.get(index);
            rows += 1;
            if (section.useFallback || section.sprite == null) {
                rows += 1;
            } else {
                rows += section.sprite.getHeight();
            }
            if (index < sections.size() - 1) {
                rows += 1;
            }
        }
        return rows;
    }

    private static int findLastVisibleSpriteSection(List<DemoSection> sections) {
        for (int index = sections.size() - 1; index >= 0; index--) {
            DemoSection section = sections.get(index);
            if (!section.useFallback && section.sprite != null && section.checkWidth) {
                return index;
            }
        }
        return -1;
    }

    private static void applyArgs(String[] args, UiConfig config) {
        for (String arg : args) {
            if ("--fullscreen".equals(arg)) {
                config.fullScreen = true;
            }
            if ("--ascii".equals(arg)) {
                config.asciiMode = true;
            }
        }
    }

    private static TerminalSize dialogSizeForScreen(Screen screen, boolean fullScreen) {
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

    private static String toggleModeLabel(boolean fullScreen) {
        return fullScreen ? "Window Mode: Fullscreen" : "Window Mode: Windowed";
    }

    private static final class UiConfig {
        private boolean fullScreen;
        private boolean asciiMode;

        private UiConfig(boolean fullScreen, boolean asciiMode) {
            this.fullScreen = fullScreen;
            this.asciiMode = asciiMode;
        }
    }

    private static final class SessionResult {
        private boolean restartRequested;
        private boolean exitRequested;

        private SessionResult(boolean restartRequested, boolean exitRequested) {
            this.restartRequested = restartRequested;
            this.exitRequested = exitRequested;
        }
    }

    private static final class DemoSection {
        private final String title;
        private final AsciiSprite sprite;
        private final String fallbackText;
        private final boolean checkWidth;
        private boolean useFallback;

        private DemoSection(String title, AsciiSprite sprite, String fallbackText, boolean checkWidth) {
            this.title = title;
            this.sprite = sprite;
            this.fallbackText = fallbackText;
            this.checkWidth = checkWidth;
            this.useFallback = false;
        }
    }

    private static final class GameSetup {
        private String playerClass;
        private String item;
        private String difficulty;

        private GameSetup(String playerClass, String item, String difficulty) {
            this.playerClass = playerClass;
            this.item = item;
            this.difficulty = difficulty;
        }
    }
}
