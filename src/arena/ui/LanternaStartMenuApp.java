package arena.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
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
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class LanternaStartMenuApp {
    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(500, 42);
    private static final Logger LOGGER = Logger.getLogger(LanternaStartMenuApp.class.getName());

    public static void main(String[] args) throws IOException {
        UiConfig config = new UiConfig(false);
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
        int viewportRows = config.fullScreen ? screen.getTerminalSize().getRows() : WINDOWED_SIZE.getRows();

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
                window.setFixedSize(WINDOWED_SIZE);
            }

            Panel panel = new Panel(new LinearLayout());
            int mainContentRows = 11;
            addVerticalPaddingTop(panel, viewportRows, mainContentRows);

            panel.addComponent(centered(new Label("╭──────────────────────────────╮")));
            panel.addComponent(centered(new Label("│        COMBAT  ARENA         │")));
            panel.addComponent(centered(new Label("╰──────────────────────────────╯")));
            panel.addComponent(new EmptySpace(new TerminalSize(1, 2)));

            panel.addComponent(centered(new Label("Display: " + (config.fullScreen ? "Fullscreen" : "Windowed"))));
            panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

            panel.addComponent(centered(new Button("New Game", () -> {
                showMessage(screen, gui, "NOTICE", "New Game is not connected yet.");
            })));
            panel.addComponent(centered(new Button("View Controls", () -> {
                showMessage(screen, gui, "CONTROLS",
                        "- Up/Down: Navigate\n" +
                        "- Enter: Confirm\n" +
                        "- Esc: Back/Close");
            })));
            panel.addComponent(centered(new Button("Options", () -> {
                openOptions(screen, gui, config, result);
                if (result.restartRequested) {
                    window.close();
                }
            })));
            panel.addComponent(centered(new Button("Exit", () -> {
                result.exitRequested = true;
                window.close();
            })));
            addVerticalPaddingBottom(panel, viewportRows, mainContentRows);

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
        TerminalSize dialogSize = dialogSizeForScreen(screen);
        optionsWindow.setFixedSize(dialogSize);

        Panel optionsPanel = new Panel(new LinearLayout());
        int optionsContentRows = 10;
        addVerticalPaddingTop(optionsPanel, dialogSize.getRows(), optionsContentRows);
        optionsPanel.addComponent(centered(new Label("╭─────────── OPTIONS ───────────╮")));
        optionsPanel.addComponent(centered(new Label("│ Toggle display mode           │")));
        optionsPanel.addComponent(centered(new Label("╰───────────────────────────────╯")));
        optionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 2)));

        final Button[] modeToggleButton = new Button[1];
        modeToggleButton[0] = new Button(toggleModeLabel(config.fullScreen), () -> {
            config.fullScreen = !config.fullScreen;
            modeToggleButton[0].setLabel(toggleModeLabel(config.fullScreen));
        });
        optionsPanel.addComponent(centered(modeToggleButton[0]));
        optionsPanel.addComponent(centered(new Label("(Apply restarts UI session)")));
        optionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        optionsPanel.addComponent(centered(new Button("Apply and Return", () -> {
            result.restartRequested = true;
            optionsWindow.close();
        })));
        optionsPanel.addComponent(centered(new Button("Back", optionsWindow::close)));
        addVerticalPaddingBottom(optionsPanel, dialogSize.getRows(), optionsContentRows);

        optionsWindow.setComponent(optionsPanel);
        gui.addWindowAndWait(optionsWindow);
    }

    private static void showMessage(Screen screen, MultiWindowTextGUI gui, String title, String text) {

        BasicWindow messageWindow = new BasicWindow();
        messageWindow.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        TerminalSize dialogSize = dialogSizeForScreen(screen);
        messageWindow.setFixedSize(dialogSize);

        Panel panel = new Panel(new LinearLayout());
        int messageLines = text.split("\\n").length;
        int messageContentRows = messageLines + 5;
        addVerticalPaddingTop(panel, dialogSize.getRows(), messageContentRows);
        panel.addComponent(centered(new Label(formatDialogHeader(title))));
        for (String line : text.split("\\n")) {
            panel.addComponent(centered(new Label(line)));
        }
        panel.addComponent(centered(new Label("╰───────────────────────────────╯")));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 2)));
        panel.addComponent(centered(new Button("OK", messageWindow::close)));
        addVerticalPaddingBottom(panel, dialogSize.getRows(), messageContentRows);

        messageWindow.setComponent(panel);
        gui.addWindowAndWait(messageWindow);
    }

    private static void applyArgs(String[] args, UiConfig config) {
        for (String arg : args) {
            if ("--fullscreen".equals(arg)) {
                config.fullScreen = true;
            }
        }
    }

    private static <T extends com.googlecode.lanterna.gui2.Component> T centered(T component) {
        component.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        return component;
    }

    private static void addVerticalPaddingTop(Panel panel, int viewportRows, int contentRows) {
        int topRows = Math.max(1, (viewportRows - contentRows) / 2);
        panel.addComponent(new EmptySpace(new TerminalSize(1, topRows)));
    }

    private static void addVerticalPaddingBottom(Panel panel, int viewportRows, int contentRows) {
        int topRows = Math.max(1, (viewportRows - contentRows) / 2);
        int bottomRows = Math.max(1, viewportRows - contentRows - topRows);
        panel.addComponent(new EmptySpace(new TerminalSize(1, bottomRows)));
    }

    private static TerminalSize dialogSizeForScreen(Screen screen) {
        TerminalSize terminalSize = screen.getTerminalSize();
        int dialogColumns = Math.min(WINDOWED_SIZE.getColumns(), Math.max(60, terminalSize.getColumns() - 2));
        int dialogRows = Math.min(WINDOWED_SIZE.getRows(), Math.max(20, terminalSize.getRows() - 2));
        return new TerminalSize(dialogColumns, dialogRows);
    }

    private static String toggleModeLabel(boolean fullScreen) {
        return fullScreen ? "Window Mode: Fullscreen" : "Window Mode: Windowed";
    }

    private static String formatDialogHeader(String title) {
        final int innerWidth = 29;
        String normalized = title == null ? "" : title.trim();
        if (normalized.length() > innerWidth) {
            normalized = normalized.substring(0, innerWidth);
        }
        int totalPadding = innerWidth - normalized.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return "╭" + "─".repeat(leftPadding) + " " + normalized + " " + "─".repeat(rightPadding) + "╮";
    }

    private static final class UiConfig {
        private boolean fullScreen;

        private UiConfig(boolean fullScreen) {
            this.fullScreen = fullScreen;
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
