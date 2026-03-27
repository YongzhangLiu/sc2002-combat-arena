package arena.ui.screen;

import arena.ui.SpriteCatalog;
import arena.ui.DialogComposer;
import arena.ui.model.ArenaUiCommand;
import arena.ui.model.ArenaViewState;
import arena.ui.model.EnemyViewState;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static arena.ui.UiScreenSupport.fittedLine;

/**
 * Host screen for arena battle UI.
 *
 * Event-driven usage:
 *  1) initialize(...)
 *  2) render(state) when engine state changes
 *  3) pollCommand() to consume pending input intents
 *  4) showOverlay(...) on victory/defeat
 *  5) close()
 */
public class ArenaBattleScreen {
    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(100, 42);
    private final ArenaLayoutCalculator layoutCalculator;
    private final Queue<ArenaUiCommand> commandQueue;

    private Screen screen;
    private MultiWindowTextGUI gui;
    private BasicWindow window;
    private boolean fullScreen;
    private boolean asciiMode;
    private ArenaViewState lastRenderedState;
    private ArenaLayoutCalculator.LayoutBounds lastLayoutBounds;
    private boolean overlayActive;
    private TerminalSize windowedLayoutSize;

    public ArenaBattleScreen() {
        this.layoutCalculator = new ArenaLayoutCalculator();
        this.commandQueue = new ArrayDeque<>();
    }

    public void initialize(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode) {
        this.screen = screen;
        this.gui = gui;
        this.fullScreen = fullScreen;
        this.asciiMode = asciiMode;
        this.overlayActive = false;
        this.windowedLayoutSize = null;

        this.window = new BasicWindow("Arena Battle");
        window.setHints(fullScreen
            ? java.util.Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.FULL_SCREEN, Window.Hint.EXPANDED)
            : java.util.Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        if (!fullScreen) {
            this.windowedLayoutSize = fitToTerminal(screen.getTerminalSize(), WINDOWED_SIZE);
            window.setFixedSize(this.windowedLayoutSize);
        }
    }

    public void render(ArenaViewState state) {
        if (window == null || gui == null || screen == null || state == null) {
            return;
        }

        this.lastRenderedState = state;
        TerminalSize size = resolveLayoutSize();
        this.lastLayoutBounds = layoutCalculator.calculate(size);

        Panel root = buildSkeletonPanel(lastLayoutBounds, state);
        window.setComponent(root);

        if (!window.isVisible()) {
            gui.addWindow(window);
        }
    }

    public void showAndWait(ArenaViewState state) {
        if (window == null || gui == null || screen == null || state == null) {
            return;
        }

        this.lastRenderedState = state;
        TerminalSize size = resolveLayoutSize();
        this.lastLayoutBounds = layoutCalculator.calculate(size);
        window.setComponent(buildSkeletonPanel(lastLayoutBounds, state));
        gui.addWindowAndWait(window);
    }

    public Optional<ArenaUiCommand> pollCommand() {
        return Optional.ofNullable(commandQueue.poll());
    }

    public void showOverlay(ArenaViewState endState) {
        if (gui == null || window == null || endState == null) {
            return;
        }

        overlayActive = true;

        BasicWindow overlay = new BasicWindow();
        overlay.setHints(java.util.Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        String title = endState.isVictory() ? "VICTORY" : "DEFEAT";
        panel.addComponent(new Label(title));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(new Label("Rounds: " + endState.getRoundNumber()));
        panel.addComponent(new Label(endState.getFeedbackMessage()));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(new Button("Close", () -> {
            overlayActive = false;
            overlay.close();
        }));

        overlay.setComponent(panel);
        gui.addWindowAndWait(overlay);
    }

    public void enqueueCommand(ArenaUiCommand command) {
        if (command != null) {
            commandQueue.offer(command);
        }
    }

    public void close() {
        if (window != null) {
            window.close();
        }
    }

    public ArenaLayoutCalculator.LayoutBounds getLastLayoutBounds() {
        return lastLayoutBounds;
    }

    public ArenaViewState getLastRenderedState() {
        return lastRenderedState;
    }

    public boolean isOverlayActive() {
        return overlayActive;
    }

    private Panel buildSkeletonPanel(ArenaLayoutCalculator.LayoutBounds bounds, ArenaViewState state) {
        Panel root = new Panel(new LinearLayout(Direction.VERTICAL));

        Panel topRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        topRow.addComponent(buildInventoryPanel(bounds.inventoryPanel(), state));
        topRow.addComponent(buildInfoPanel(bounds.infoPanel(), state));
        topRow.addComponent(buildStatusPanel(bounds.statusPanel(), state));

        root.addComponent(topRow);
        root.addComponent(buildArenaPanel(bounds.arenaPanel(), state));
        root.addComponent(buildActionBar(bounds.actionBar(), state));
        return root;
    }

    private Component buildInventoryPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int innerWidth = Math.max(8, rect.width() - 2);
        int spacer = 3;
        int leftWidth = Math.max(4, (innerWidth - spacer) / 2);
        int rightWidth = Math.max(4, innerWidth - spacer - leftWidth);

        List<String> itemLines = new ArrayList<>();
        itemLines.add("Item");
        itemLines.addAll(loadInventorySprite(state, leftWidth, 5));

        List<String> skillLines = new ArrayList<>();
        skillLines.add("Special");
        skillLines.addAll(loadSkillSprite(state, rightWidth, 5));

        int bodyRows = Math.max(1, rect.height() - 2);
        List<String> body = new ArrayList<>();
        for (int row = 0; row < bodyRows; row++) {
            String left = row < itemLines.size() ? fittedLine(itemLines.get(row), leftWidth) : "";
            String right = row < skillLines.size() ? fittedLine(skillLines.get(row), rightWidth) : "";
            body.add(padRight(left, leftWidth) + " ".repeat(spacer) + padRight(right, rightWidth));
        }

        return buildUtilitySection("Inventory", rect, body);
    }

    private Component buildStatusPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int contentWidth = Math.max(8, rect.width() - 2);
        String playerHp = state.getPlayerState() == null
            ? "-"
            : state.getPlayerState().getCurrentHp() + "/" + state.getPlayerState().getMaxHp();

        List<String> body = List.of(
            fittedLine("Player HP: " + playerHp, contentWidth),
            fittedLine("Enemies: " + state.getAliveEnemies().size(), contentWidth),
            fittedLine("Target: " + (state.getCurrentTargetIndex() + 1), contentWidth)
        );
        return buildUtilitySection("Status", rect, body);
    }

    private Component buildInfoPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int contentWidth = Math.max(8, rect.width() - 2);
        int maxLogs = Math.max(1, rect.height() - 4);
        List<String> body = new ArrayList<>();
        body.add(fittedLine("Round: " + state.getRoundNumber(), contentWidth));
        body.add(fittedLine("Turn: " + state.getTurnOwnerName(), contentWidth));
        for (int i = 0; i < state.getCombatLog().size() && i < maxLogs; i++) {
            body.add(fittedLine(state.getCombatLog().get(i), contentWidth));
        }
        body.add(fittedLine(state.getFeedbackMessage(), contentWidth));
        return buildUtilitySection("Info", rect, body);
    }

    private Component buildArenaPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int contentWidth = Math.max(20, rect.width() - 2);
        int arenaRows = Math.max(6, rect.height() - 1);

        Panel content = new Panel(new LinearLayout(Direction.VERTICAL));

        int actorRows = Math.max(3, arenaRows - 1);
        int playerWidth = Math.max(12, Math.min(24, contentWidth / 3));
        int gap = Math.max(2, Math.min(6, contentWidth / 18));
        int enemyWidth = contentWidth - playerWidth - gap;
        if (enemyWidth < 8) {
            gap = Math.max(1, gap - (8 - enemyWidth));
            enemyWidth = Math.max(8, contentWidth - playerWidth - gap);
        }

        Panel actorRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        actorRow.addComponent(buildPlayerBlock(playerWidth, actorRows, state));
        actorRow.addComponent(new EmptySpace(new TerminalSize(gap, 1)));
        actorRow.addComponent(buildEnemyStrip(enemyWidth, actorRows, state));
        content.addComponent(actorRow);

        for (String tileLine : loadArenaBaseLine(contentWidth, 1)) {
            content.addComponent(new Label(fittedLine(tileLine, contentWidth)));
        }
        content.setPreferredSize(new TerminalSize(rect.width(), rect.height()));
        return content;
    }

    private Component buildActionBar(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int actionCount = Math.max(1, state.getAvailableActions().size()) + 1;
        Panel row = new Panel(new GridLayout(actionCount));
        int buttonWidth = Math.max(8, (rect.width() / actionCount) - 2);

        for (String action : state.getAvailableActions()) {
            row.addComponent(new Button(fittedLine(action, buttonWidth), () -> enqueueCommand(new ArenaUiCommand.SelectAction(action))));
        }
        row.addComponent(new Button("Back to Menu", this::close));
        row.setPreferredSize(new TerminalSize(rect.width(), rect.height()));
        return row;
    }

    private Component buildPlayerBlock(int width, int height, ArenaViewState state) {
        Panel block = new Panel(new LinearLayout(Direction.VERTICAL));
        if (state.getPlayerState() == null) {
            block.addComponent(new Label("No player"));
            block.setPreferredSize(new TerminalSize(width, height));
            return block;
        }

        int spriteRows = Math.max(1, height);
        List<String> spriteLines = loadSprite("player", state.getPlayerState().getSpriteKey(), Math.max(6, width), spriteRows);
        int topPadding = Math.max(0, height - spriteLines.size());
        for (int i = 0; i < topPadding; i++) {
            block.addComponent(new Label(""));
        }

        for (String line : spriteLines) {
            block.addComponent(new Label(fittedLine(line, width)));
        }
        block.setPreferredSize(new TerminalSize(width, height));
        return block;
    }

    private Component buildEnemyStrip(int width, int height, ArenaViewState state) {
        if (state.getAliveEnemies().isEmpty()) {
            Panel empty = new Panel(new LinearLayout(Direction.VERTICAL));
            empty.addComponent(new Label("No enemies alive"));
            empty.setPreferredSize(new TerminalSize(width, height));
            return empty;
        }

        int enemyCount = Math.max(1, state.getAliveEnemies().size());
        int slotWidth = Math.max(8, Math.min(19, (width / enemyCount) + 3));
        if (slotWidth * enemyCount > width) {
            slotWidth = Math.max(6, width / enemyCount);
        }

        int spacing = 0;
        if (enemyCount > 1) {
            spacing = Math.min(2, Math.max(0, (width - (slotWidth * enemyCount)) / (enemyCount - 1)));
        }
        int usedWidth = (slotWidth * enemyCount) + (spacing * Math.max(0, enemyCount - 1));
        int leftPadding = Math.max(0, width - usedWidth);

        Panel enemyRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        if (leftPadding > 0) {
            enemyRow.addComponent(new EmptySpace(new TerminalSize(leftPadding, 1)));
        }

        int spriteRows = Math.max(1, height - 4);
        for (int index = 0; index < state.getAliveEnemies().size(); index++) {
            EnemyViewState enemy = state.getAliveEnemies().get(index);
            enemyRow.addComponent(buildEnemySlot(enemy, index, state.getCurrentTargetIndex(), slotWidth, height, spriteRows));
            if (index < state.getAliveEnemies().size() - 1 && spacing > 0) {
                enemyRow.addComponent(new EmptySpace(new TerminalSize(spacing, 1)));
            }
        }
        enemyRow.setPreferredSize(new TerminalSize(width, height));
        return enemyRow;
    }

    private Component buildEnemySlot(EnemyViewState enemy, int index, int selectedIndex, int slotWidth, int slotHeight, int spriteRows) {
        int contentWidth = Math.max(5, slotWidth - 1);
        Panel content = new Panel(new LinearLayout(Direction.VERTICAL));

        String targetLabel;
        if (contentWidth >= 10) {
            targetLabel = index == selectedIndex ? "SELECTED" : "SELECT";
        } else {
            targetLabel = index == selectedIndex ? "SEL*" : "SEL";
        }
        boolean hasEffects = !enemy.getActiveEffects().isEmpty();
        int fixedLines = hasEffects ? 3 : 2;
        List<String> spriteLines = loadEnemySpriteLines(enemy, contentWidth, spriteRows);
        int topPadding = Math.max(0, slotHeight - fixedLines - spriteLines.size());
        for (int i = 0; i < topPadding; i++) {
            content.addComponent(new Label(""));
        }

        content.addComponent(new Button(targetLabel, () -> enqueueCommand(new ArenaUiCommand.SelectTarget(index))));
        if (hasEffects) {
            content.addComponent(new Label(fittedLine(effectLine(enemy), contentWidth)));
        }
        content.addComponent(new Label(fittedLine(hpLine(enemy, contentWidth), contentWidth)));
        for (String spriteLine : spriteLines) {
            content.addComponent(new Label(fittedLine(spriteLine, contentWidth)));
        }

        content.setPreferredSize(new TerminalSize(Math.max(6, slotWidth), Math.max(6, slotHeight)));
        return content;
    }

    private List<String> loadEnemySpriteLines(EnemyViewState enemy, int maxWidth, int maxRows) {
        try {
            List<String> lines = SpriteCatalog.load("enemy", enemy.getSpriteKey(), "normal").getLines();
            return EnemySpriteClipper.clipRight(lines, maxWidth, maxRows);
        } catch (IOException exception) {
            return List.of("[" + enemy.getType() + "]");
        }
    }

    private String effectLine(EnemyViewState enemy) {
        if (enemy.getActiveEffects().isEmpty()) {
            return "Effects: -";
        }
        String joined = enemy.getActiveEffects().stream()
            .map(effect -> effect.getIconSymbol() == null || effect.getIconSymbol().isBlank()
                ? effect.getEffectName()
                : effect.getIconSymbol())
            .reduce((left, right) -> left + " " + right)
            .orElse("-");
        return "Effects: " + joined;
    }

    private String hpLine(EnemyViewState enemy, int maxWidth) {
        int barWidth = Math.max(4, Math.min(10, maxWidth - 10));
        int filled = Math.max(0, Math.min(barWidth, (int) Math.round((enemy.getCurrentHp() * barWidth) / (double) enemy.getMaxHp())));
        String bar = "█".repeat(filled) + "-".repeat(Math.max(0, barWidth - filled));
        return bar + " " + enemy.getCurrentHp() + "/" + enemy.getMaxHp();
    }

    private Component buildUtilitySection(String title, ArenaLayoutCalculator.Rect rect, List<String> contentLines) {
        int innerWidth = Math.max(1, rect.width() - 2);
        int bodyRows = Math.max(1, rect.height() - 2);

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label(formatEmbeddedTopBorder(title, innerWidth)));

        for (int i = 0; i < bodyRows; i++) {
            String line = i < contentLines.size() ? contentLines.get(i) : "";
            panel.addComponent(new Label(formatBodyBorderLine(line, innerWidth)));
        }

        panel.addComponent(new Label(DialogComposer.formatBottomBorder(innerWidth, asciiMode)));
        panel.setPreferredSize(new TerminalSize(rect.width(), rect.height()));
        return panel;
    }

    private String formatEmbeddedTopBorder(String title, int borderWidth) {
        int innerWidth = Math.max(1, borderWidth);
        String horizontal = asciiMode ? "-" : "─";
        String topLeft = asciiMode ? "+" : "╭";
        String topRight = asciiMode ? "+" : "╮";

        String normalized = title == null ? "" : title.trim();
        String label = " " + normalized + " ";
        if (label.length() > innerWidth) {
            label = fittedLine(label, innerWidth);
        }

        int totalPadding = Math.max(0, innerWidth - label.length());
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;

        return topLeft
            + horizontal.repeat(leftPadding)
            + label
            + horizontal.repeat(rightPadding)
            + topRight;
    }

    private String formatBodyBorderLine(String text, int innerWidth) {
        String vertical = asciiMode ? "|" : "│";
        String content = fittedLine(text == null ? "" : text, innerWidth);
        return vertical + padRight(content, innerWidth) + vertical;
    }

    private String padRight(String value, int width) {
        String safe = value == null ? "" : value;
        if (safe.length() >= width) {
            return safe.substring(0, width);
        }
        return safe + " ".repeat(width - safe.length());
    }

    private TerminalSize resolveLayoutSize() {
        if (!fullScreen) {
            if (windowedLayoutSize != null) {
                return windowedLayoutSize;
            }
            return fitToTerminal(screen.getTerminalSize(), WINDOWED_SIZE);
        }
        return screen.getTerminalSize();
    }

    private TerminalSize fitToTerminal(TerminalSize terminalSize, TerminalSize preferred) {
        int availableColumns = Math.max(20, terminalSize.getColumns() - 2);
        int availableRows = Math.max(10, terminalSize.getRows() - 2);
        int columns = Math.min(preferred.getColumns(), availableColumns);
        int rows = Math.min(preferred.getRows(), availableRows);
        return new TerminalSize(columns, rows);
    }

    private List<String> loadInventorySprite(ArenaViewState state, int maxWidth, int maxRows) {
        if (state.getAvailableItems().isEmpty()) {
            return List.of("[no item]");
        }
        String itemName = state.getAvailableItems().get(0).toLowerCase().replace(' ', '_');
        return loadSprite("item", itemName, maxWidth, maxRows);
    }

    private List<String> loadSkillSprite(ArenaViewState state, int maxWidth, int maxRows) {
        if (state.getPlayerState() == null || state.getPlayerState().getType() == null) {
            return List.of("[skill]");
        }
        String skillKey = state.getPlayerState().getType().toLowerCase() + "_special";
        return loadSprite("skill", skillKey, maxWidth, maxRows);
    }

    private List<String> loadArenaBaseLine(int maxWidth, int rows) {
        try {
            return EnemySpriteClipper.clipRight(SpriteCatalog.loadArenaStrip("forest", maxWidth).getLines(), maxWidth, rows);
        } catch (IOException exception) {
            return List.of("_".repeat(Math.max(1, maxWidth)));
        }
    }

    private List<String> loadSprite(String category, String name, int maxWidth, int maxRows) {
        try {
            return EnemySpriteClipper.clipRight(SpriteCatalog.load(category, name, "normal").getLines(), maxWidth, maxRows);
        } catch (IOException exception) {
            return List.of("[" + name + "]");
        }
    }
}
