package arena.ui;

import arena.ui.screen.PlayerSelectionScreen;
import arena.ui.util.DialogComposer;
import arena.ui.screen.ItemSelectionScreen;
import arena.ui.screen.EnemyInformationScreen;
import arena.ui.screen.DifficultySelectionScreen;
import arena.ui.screen.ArenaBattleScreen;
import arena.ui.screen.ArenaPreviewStateFactory;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.DelegatingTheme;
import com.googlecode.lanterna.graphics.DelegatingThemeDefinition;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.graphics.ThemeStyle;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.ExtendedTerminal;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import arena.ui.util.ScreenUtil;
import java.util.Arrays;
import java.util.function.Consumer;

import static arena.ui.util.ScreenUtil.dialogSizeForScreen;
import static arena.ui.util.TextFormatUtil.fittedLine;
/*
 * Spins up start menu and calls setup screens
*/
public class StartMenu {
    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(100, 42);

    public static void main(String[] args) throws IOException {
        UiConfig config = new UiConfig(false, false);
        applyArgs(args, config);

        launch(config.fullScreen, config.asciiMode, null);
    }

    public static void launch(boolean fullScreen, boolean asciiMode, Consumer<GameSetup> onSetupReady) throws IOException {
        launch(null, null, fullScreen, asciiMode, onSetupReady);
    }

    public static void launch(Screen sharedScreen, MultiWindowTextGUI sharedGui, boolean fullScreen, boolean asciiMode, Consumer<GameSetup> onSetupReady) throws IOException {
        UiConfig config = new UiConfig(fullScreen, asciiMode);

        boolean keepRunning = true;
        while (keepRunning) {
            keepRunning = runSession(sharedScreen, sharedGui, config, onSetupReady);
        }
    }

    private static Screen createLocalScreen(UiConfig config) throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        if (!config.fullScreen) {
            terminalFactory.setInitialTerminalSize(WINDOWED_SIZE);
        }
        Screen screen = terminalFactory.createScreen();
        screen.startScreen();
        ScreenUtil.setMouseReporting(screen, true);
        return screen;
    }

    private static MultiWindowTextGUI createLocalGui(Screen screen) {
        MultiWindowTextGUI gui = new MultiWindowTextGUI(
            screen,
            new DefaultWindowManager(),
            new EmptySpace(TextColor.ANSI.DEFAULT)
        );
        applyUniformButtonTheme(gui);
        return gui;
    }

    private static boolean runSession(Screen sharedScreen, MultiWindowTextGUI sharedGui, UiConfig config, Consumer<GameSetup> onSetupReady) throws IOException {
        final Screen screen = sharedScreen != null ? sharedScreen : createLocalScreen(config);
        final MultiWindowTextGUI gui = sharedGui != null ? sharedGui : createLocalGui(screen);
        boolean ownsTerminal = false; // Always assume shared terminal in master flow


        SessionResult result = new SessionResult(false, false);
        TerminalSize terminalSize = screen.getTerminalSize();
        TerminalSize activeWindowSize = config.fullScreen
            ? terminalSize
            : fitToTerminal(terminalSize, WINDOWED_SIZE);
        int viewportRows = activeWindowSize.getRows();
        int viewportColumns = activeWindowSize.getColumns();

        try {
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
                startNewGameFlow(screen, gui, config.fullScreen, config.asciiMode, onSetupReady);
            })));
            panel.addComponent(DialogComposer.centered(new Button("View Controls", () -> {
                showMessage(screen, gui, config.fullScreen, config.asciiMode, "CONTROLS",
                        "- Up/Down: Navigate\n" +
                        "- Mouse: Click buttons\n" +
                        "- Enter: Confirm\n" +
                        "- Esc: Back/Close");
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
            if (ownsTerminal && screen != null) {
                ScreenUtil.setMouseReporting(screen, false);
                screen.stopScreen();
            }
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

    static void startNewGameFlow(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode, Consumer<GameSetup> onSetupReady) {
        GameSetup setup = new GameSetup("Warrior", "Potion", "Easy");

        int currentScreen = 0; // 0: player, 1: item, 2: enemy, 3: difficulty
        while (currentScreen >= 0 && currentScreen < 4) {
            int result = currentScreen == 0 ? PlayerSelectionScreen.open(screen, gui, fullScreen, asciiMode, setup)
                       : currentScreen == 1 ? ItemSelectionScreen.open(screen, gui, fullScreen, asciiMode, setup)
                       : currentScreen == 2 ? EnemyInformationScreen.open(screen, gui, fullScreen, asciiMode)
                       : DifficultySelectionScreen.open(screen, gui, fullScreen, asciiMode, setup);

            if (result == -1) {
                currentScreen--;
            } else if (result == 1) {
                currentScreen++;
            } else {
                return;
            }
        }

        if (onSetupReady != null) {
            onSetupReady.accept(setup);
        }
    }

    private static String buildSetupSummary(GameSetup setup) {
        return "Player: " + setup.playerClass + "\n"
            + "Item: " + setup.item + "\n"
            + "Difficulty: " + setup.difficulty + "\n"
            + "\n"
            + "Next: connect setup to battle engine flow.";
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

    public static void applyUniformButtonTheme(MultiWindowTextGUI gui) {
        Theme baseTheme = LanternaThemes.getDefaultTheme();
        gui.setTheme(new DelegatingTheme(baseTheme) {
            @Override
            public ThemeDefinition getDefinition(Class<?> componentClass) {
                ThemeDefinition original = super.getDefinition(componentClass);
                if (!Button.class.equals(componentClass)) {
                    return original;
                }
                return new DelegatingThemeDefinition(original) {
                    @Override
                    public ThemeStyle getPreLight() {
                        return getNormal();
                    }
                };
            }
        });
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

}
