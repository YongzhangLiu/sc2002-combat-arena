package arena.ui.screen;

import arena.ui.sprite.AsciiSprite;
import arena.ui.sprite.SpriteCatalog;
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
import com.googlecode.lanterna.TextColor;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static arena.ui.util.TextFormatUtil.fittedLine;

public class ArenaBattleScreen {
    public interface ActionCallbacks {
        void onBasicAttack(int targetIndex);
        void onDefend();
        void onUseItem(int itemIndex, int targetIndex);
        void onSpecialSkill(int targetIndex);
        void onBackToMenu();
    }

    private static final TerminalSize WINDOWED_SIZE = new TerminalSize(120, 42);

    private final ArenaLayoutCalculator layoutCalculator;
    private ActionCallbacks callbacks;

    private Screen screen;
    private MultiWindowTextGUI gui;
    private BasicWindow window;
    private boolean fullScreen;
    private boolean asciiMode;
    private ArenaViewState lastRenderedState;
    private ArenaLayoutCalculator.LayoutBounds lastLayoutBounds;
    private TerminalSize windowedLayoutSize;
    private int uiSelectedTargetIndex;
    private boolean showInfoButtons = false;

    public ArenaBattleScreen() {
        this.layoutCalculator = new ArenaLayoutCalculator();
    }

    public void setCallbacks(ActionCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void initialize(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode) {
        this.screen = screen;
        this.gui = gui;
        this.fullScreen = fullScreen;
        this.asciiMode = asciiMode;
        this.windowedLayoutSize = null;
        this.uiSelectedTargetIndex = 0;

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
        this.lastLayoutBounds = layoutCalculator.calculate(resolveLayoutSize());

        window.setComponent(buildSkeletonPanel(lastLayoutBounds, state));
        if (!window.isVisible()) {
            gui.addWindow(window);
        }
    }

    public void showAndWait(ArenaViewState state) {
        if (window == null || gui == null || screen == null || state == null) {
            return;
        }

        this.lastRenderedState = state;
        this.lastLayoutBounds = layoutCalculator.calculate(resolveLayoutSize());
        window.setComponent(buildSkeletonPanel(lastLayoutBounds, state));
        gui.addWindowAndWait(window);
    }

    private void showInfoDialog(String title, String description, String stats) {
        BasicWindow infoWindow = new BasicWindow();
        infoWindow.setHints(java.util.Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING));
        
        Panel p = new Panel(new LinearLayout(Direction.VERTICAL));
        Label titleLabel = new Label(title);
        titleLabel.setForegroundColor(TextColor.ANSI.CYAN);
        p.addComponent(titleLabel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        p.addComponent(new Label(description));
        p.addComponent(new Label(stats));
        Button closeBtn = new Button("Close", infoWindow::close);
        p.addComponent(closeBtn.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        
        arena.ui.util.CustomBorder customBorder = new arena.ui.util.CustomBorder(asciiMode);
        customBorder.setComponent(p);
        infoWindow.setComponent(customBorder);
        
        if (gui != null) {
            gui.addWindowAndWait(infoWindow);
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

    private Panel buildSkeletonPanel(ArenaLayoutCalculator.LayoutBounds bounds, ArenaViewState state) {
        Panel root = new Panel(new LinearLayout(Direction.VERTICAL));
        if (bounds.inventoryPanel().row() > 0) {
            root.addComponent(new EmptySpace(new TerminalSize(1, bounds.inventoryPanel().row())));
        }

        Panel topRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        topRow.addComponent(buildInventoryPanel(bounds.inventoryPanel(), state));

        int gapInventoryToInfo = Math.max(0, bounds.infoPanel().column() - bounds.inventoryPanel().right());
        if (gapInventoryToInfo > 0) {
            topRow.addComponent(new EmptySpace(new TerminalSize(gapInventoryToInfo, 1)));
        }

        topRow.addComponent(buildInfoPanel(bounds.infoPanel(), state));

        int gapInfoToStatus = Math.max(0, bounds.statusPanel().column() - bounds.infoPanel().right());
        if (gapInfoToStatus > 0) {
            topRow.addComponent(new EmptySpace(new TerminalSize(gapInfoToStatus, 1)));
        }

        topRow.addComponent(buildStatusPanel(bounds.statusPanel(), state));
        topRow.setPreferredSize(new TerminalSize(bounds.canvas().width(), bounds.inventoryPanel().height()));

        root.addComponent(topRow);
        root.addComponent(buildArenaPanel(bounds.arenaPanel(), state));
        root.addComponent(buildActionBar(bounds.actionBar(), state));
        return root;
    }

    private Component buildInventoryPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int innerWidth = Math.max(8, rect.width() - 2);
        int spacer = 2;
        int leftWidth = Math.max(4, (innerWidth - spacer) / 2);
        int rightWidth = Math.max(4, innerWidth - spacer - leftWidth);

        String slot1Name = getItemNameAt(state, 0);
        String slot2Name = getItemNameAt(state, 1);

        List<String> slot1Lines = new ArrayList<>();
        slot1Lines.add(" Item 1");
        slot1Lines.addAll(loadInventorySlotSprite(slot1Name, leftWidth, 3));
        slot1Lines.add(fittedLine(slot1Name, leftWidth));

        List<String> slot2Lines = new ArrayList<>();
        slot2Lines.add(" Item 2");
        slot2Lines.addAll(loadInventorySlotSprite(slot2Name, rightWidth, 3));
        slot2Lines.add(fittedLine(slot2Name, rightWidth));

        String skillName = getSkillDisplayName(state);
        String skillStatus;
        if (state.getPlayerState() == null) {
            skillStatus = "Cooldown: N/A";
        } else {
            int cooldown = state.getPlayerState().getSpecialSkillCooldown();
            skillStatus = cooldown > 0 ? "Cooldown: " + cooldown : "Cooldown: Ready";
        }
        slot1Lines.add(fittedLine(skillName, leftWidth));
        slot2Lines.add(fittedLine(skillStatus, rightWidth));
        slot1Lines.addAll(loadSkillSprite(state, leftWidth, 2));

        int bodyRows = Math.max(1, rect.height() - 2);
        List<String> body = new ArrayList<>();
        for (int row = 0; row < bodyRows; row++) {
            String left = row < slot1Lines.size() ? slot1Lines.get(row) : "";
            String right = row < slot2Lines.size() ? slot2Lines.get(row) : "";
            body.add(padRight(left, leftWidth) + " ".repeat(spacer) + padRight(right, rightWidth));
        }

        Component base = buildUtilitySection("Inventory", rect, body, false);
        
        if (!showInfoButtons) {
            return base;
        }

        com.googlecode.lanterna.gui2.Panel wrapper = new com.googlecode.lanterna.gui2.Panel(new com.googlecode.lanterna.gui2.AbsoluteLayout());
        base.setPosition(new com.googlecode.lanterna.TerminalPosition(0, 0));
        base.setSize(new com.googlecode.lanterna.TerminalSize(rect.width(), rect.height()));
        wrapper.addComponent(base);

        int itemInfoRow = Math.min(bodyRows - 1, 1);
        com.googlecode.lanterna.gui2.Button itemsInfo = createInfoButton("(i)", () -> {
            String slot1Desc = getItemDescriptionAt(state, 0);
            String slot2Desc = getItemDescriptionAt(state, 1);
            showInfoDialog(
                "Inventory Items",
                "Item 1: " + slot1Name + "\nItem 2: " + slot2Name,
                "Item 1 detail: " + slot1Desc + "\nItem 2 detail: " + slot2Desc
            );
        });
        itemsInfo.setPosition(new com.googlecode.lanterna.TerminalPosition(1 + Math.max(0, leftWidth / 2 - 2), 1 + itemInfoRow));
        itemsInfo.setSize(new com.googlecode.lanterna.TerminalSize(5, 1));
        wrapper.addComponent(itemsInfo);

        com.googlecode.lanterna.gui2.Button skillInfo = createInfoButton("(i)", () -> {
            String skillTitle = getSkillDisplayName(state);
            String skillDescription = state.getSpecialSkillDescription() != null && !state.getSpecialSkillDescription().isBlank()
                ? state.getSpecialSkillDescription()
                : "No special skill information available.";
            String cooldownText = "Cooldown: ";
            if (state.getPlayerState() == null) {
                cooldownText += "N/A";
            } else {
                int cooldown = state.getPlayerState().getSpecialSkillCooldown();
                cooldownText += cooldown > 0 ? cooldown : "Ready";
            }
            showInfoDialog(skillTitle, skillDescription, cooldownText);
        });
        int skillLabelRow = Math.min(bodyRows - 1, 5);
        int skillIconColumn = 1 + Math.max(0, Math.min(leftWidth - 3, skillName.length() + 1));
        skillInfo.setPosition(new com.googlecode.lanterna.TerminalPosition(skillIconColumn, 1 + skillLabelRow));
        skillInfo.setSize(new com.googlecode.lanterna.TerminalSize(5, 1));
        wrapper.addComponent(skillInfo);

        wrapper.setPreferredSize(new com.googlecode.lanterna.TerminalSize(rect.width(), rect.height()));
        return wrapper;
    }

    private Component buildStatusPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        String playerHp = state.getPlayerState() == null
            ? "-"
            : state.getPlayerState().getCurrentHp() + "/" + state.getPlayerState().getMaxHp();
        int selectedIndex = getSelectedTargetIndex(state.getAliveEnemies().size());
        String targetDisplay = state.getAliveEnemies().isEmpty() ? "-" : String.valueOf(selectedIndex + 1);

        List<String> body = List.of(
            "Player HP: " + playerHp,
            "Enemies: " + state.getAliveEnemies().size(),
            "Target: " + targetDisplay
        );
        return buildUtilitySection("Status", rect, body, false);
    }

    private Component buildInfoPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        List<String> source = new ArrayList<>();
        source.add("Round: " + state.getRoundNumber());
        source.add("Turn: " + state.getTurnOwnerName());
        source.addAll(state.getCombatLog());
        source.add(state.getFeedbackMessage());

        return buildUtilitySection("Logs", rect, source, true);
    }

    private Component buildArenaPanel(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int contentWidth = Math.max(20, rect.width() - 2);
        int arenaRows = Math.max(6, rect.height() - 1);

        Panel content = new Panel(new LinearLayout(Direction.VERTICAL));

        int cloudRows = Math.max(0, Math.min(6, arenaRows - 4));
        int actorRows = Math.max(3, arenaRows - cloudRows - 1);

        for (String cloudLine : loadCloudLines(contentWidth, cloudRows)) {
            content.addComponent(new Label(fittedLine(cloudLine, contentWidth)));
        }

        int enemyCount = Math.max(1, state.getAliveEnemies().size());
        int minEnemyContentWidth = getMaxEnemySpriteWidth(state);
        int minEnemySlotWidth = Math.max(8, minEnemyContentWidth + 1);
        int minEnemySpacing = enemyCount > 1 ? 1 : 0;
        int minEnemyWidth = (minEnemySlotWidth * enemyCount) + (minEnemySpacing * Math.max(0, enemyCount - 1));

        int desiredPlayerWidth = Math.max(12, Math.min(24, contentWidth / 3));
        int minPlayerWidth = 8;
        int gap = Math.max(1, Math.min(4, contentWidth / 20));

        int maxGapForMinimum = Math.max(0, contentWidth - minPlayerWidth - minEnemyWidth);
        gap = Math.min(gap, maxGapForMinimum);

        int maxPlayerWidth = Math.max(minPlayerWidth, contentWidth - gap - minEnemyWidth);
        int playerWidth = Math.max(minPlayerWidth, Math.min(desiredPlayerWidth, maxPlayerWidth));
        int enemyWidth = Math.max(8, contentWidth - playerWidth - gap);

        Panel actorRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        actorRow.addComponent(buildPlayerBlock(playerWidth, actorRows, state));
        actorRow.addComponent(new EmptySpace(new TerminalSize(gap, 1)));
        actorRow.addComponent(buildEnemyStrip(enemyWidth, actorRows, state, minEnemyContentWidth));
        content.addComponent(actorRow);

        for (String tileLine : loadArenaBaseLine(contentWidth, 1)) {
            content.addComponent(new Label(fittedLine(tileLine, contentWidth)));
        }

        content.setPreferredSize(new TerminalSize(rect.width(), rect.height()));
        return content;
    }

    private Component buildActionBar(ArenaLayoutCalculator.Rect rect, ArenaViewState state) {
        int actionCount = 6; // BasicAttack, Defend, UseItem, SpecialSkill, ShowInfo, Back
        Panel row = new Panel(new GridLayout(actionCount));
        int buttonWidth = Math.max(8, (rect.width() / actionCount) - 2);

        int targetIdx = getSelectedTargetIndex(state.getAliveEnemies().size());

        row.addComponent(createSafeButton(fittedLine("Basic Attack", buttonWidth), () -> {
            if (callbacks != null) callbacks.onBasicAttack(targetIdx);
        }));
        row.addComponent(createSafeButton(fittedLine("Defend", buttonWidth), () -> {
            if (callbacks != null) callbacks.onDefend();
        }));
        
        boolean hasItems = state.getPlayerState() != null && state.getPlayerState().hasItems();
        String itemLabel = hasItems ? "Use Item" : "(Empty)";
        row.addComponent(createSafeButton(fittedLine(itemLabel, buttonWidth), () -> {
            if (hasItems) {
                showItemSelectionDialog(state, targetIdx);
            }
        }));
        
        boolean skillAvailable = state.getPlayerState() != null && state.getPlayerState().isSpecialSkillAvailable();
        String skillLabel = "Special Skill";
        row.addComponent(createSafeButton(fittedLine(skillLabel, buttonWidth), () -> {
            if (!skillAvailable) {
                showSimpleDialog("SKILL NOT READY");
                return;
            }
            if (callbacks != null) {
                callbacks.onSpecialSkill(targetIdx);
            }
        }));

        String toggleInfoLabel = showInfoButtons ? "Hide Info" : "Show Info";
        row.addComponent(createSafeButton(fittedLine(toggleInfoLabel, buttonWidth), () -> {
            showInfoButtons = !showInfoButtons;
            render(lastRenderedState);
        }));
        
        row.addComponent(new Button("Back to Menu", () -> {
            if (callbacks != null) callbacks.onBackToMenu();
            close();
        }));
        row.setPreferredSize(new TerminalSize(rect.width(), rect.height()));
        return row;
    }

    // Strip `< >` wrapping in buttons and reapply theming
    private Button createInfoButton(String label, Runnable action) {
        Button b = createSafeButton(label, action);
        b.setLayoutData(com.googlecode.lanterna.gui2.LinearLayout.createLayoutData(com.googlecode.lanterna.gui2.LinearLayout.Alignment.Center));
        return b;
    }

    private void showSimpleDialog(String message) {
        if (gui == null) {
            return;
        }
        BasicWindow window = new BasicWindow();
        window.setHints(java.util.Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING));
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label(message));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(new Button("OK", window::close));
        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    // Button that ignores mouse click release events to prevent accidental double activations due to lanterna's mouse event handling
    private Button createSafeButton(String label, Runnable action) {
        return new Button(label, action) {
            @Override
            public com.googlecode.lanterna.gui2.Interactable.Result handleKeyStroke(com.googlecode.lanterna.input.KeyStroke keyStroke) {
                if (keyStroke.getKeyType() == com.googlecode.lanterna.input.KeyType.MouseEvent) {
                    com.googlecode.lanterna.input.MouseAction mouseEvent = (com.googlecode.lanterna.input.MouseAction) keyStroke;
                    if (mouseEvent.getActionType() == com.googlecode.lanterna.input.MouseActionType.CLICK_RELEASE) {
                        return com.googlecode.lanterna.gui2.Interactable.Result.HANDLED;
                    }
                }
                return super.handleKeyStroke(keyStroke);
            }
        };
    }

    private Component buildPlayerBlock(int width, int height, ArenaViewState state) {
        Panel block = new Panel(new LinearLayout(Direction.VERTICAL));
        if (state.getPlayerState() == null) {
            block.addComponent(new Label("No player"));
            block.setPreferredSize(new TerminalSize(width, height));
            return block;
        }

        int fixedLines = showInfoButtons ? 2 : 1;
        Integer floatingDamage = state.getPlayerState().getFloatingDamage();
        if (floatingDamage != null) {
            fixedLines++;
        }
        
        int spriteRows = Math.max(1, height - fixedLines);
        List<String> spriteLines = loadSprite("player", state.getPlayerState().getSpriteKey(), Math.max(6, width), spriteRows);
        int topPadding = Math.max(0, height - fixedLines - spriteLines.size());
        for (int i = 0; i < topPadding; i++) {
            block.addComponent(new Label(""));
        }

        if (floatingDamage != null) {
            block.addComponent(new Label(fittedLine("  -" + floatingDamage, width)).setForegroundColor(com.googlecode.lanterna.TextColor.ANSI.RED));
        }

        block.addComponent(new Label(fittedLine(" " + hpLine(state.getPlayerState().getCurrentHp(), state.getPlayerState().getMaxHp(), width), width)));

        if (showInfoButtons) {
            Button infoBtn = createInfoButton("(ℹ)", () -> {
                showInfoDialog(
                    state.getPlayerState().getName() + " (" + state.getPlayerState().getType() + ")",
                    state.getPlayerState().getDescription(),
                    "HP: " + state.getPlayerState().getCurrentHp() + "/" + state.getPlayerState().getMaxHp() + "\n" +
                    "Attack: " + state.getPlayerState().getAttack() + "\n" +
                    "Defense: " + state.getPlayerState().getDefense() + "\n" +
                    "Speed: " + state.getPlayerState().getSpeed()
                );
            });
            block.addComponent(infoBtn);
        }

        for (String line : spriteLines) {
            block.addComponent(new Label(fittedLine(line, width)));
        }

        block.setPreferredSize(new TerminalSize(width, height));
        return block;
    }

    private Component buildEnemyStrip(int width, int height, ArenaViewState state, int minEnemyContentWidth) {
        if (state.getAliveEnemies().isEmpty()) {
            Panel empty = new Panel(new LinearLayout(Direction.VERTICAL));
            empty.addComponent(new Label("No enemies alive"));
            empty.setPreferredSize(new TerminalSize(width, height));
            return empty;
        }

        int enemyCount = Math.max(1, state.getAliveEnemies().size());
        int minimumSlotWidth = Math.max(8, minEnemyContentWidth + 1);
        int slotWidth = Math.max(minimumSlotWidth, width / enemyCount);

        if (slotWidth * enemyCount > width) {
            slotWidth = Math.max(6, width / enemyCount);
        }

        int spacing = 0;
        if (enemyCount > 1) {
            spacing = Math.min(2, Math.max(0, (width - (slotWidth * enemyCount)) / (enemyCount - 1)));
        }

        while (enemyCount > 1
            && slotWidth > minimumSlotWidth
            && (slotWidth * enemyCount) + (spacing * (enemyCount - 1)) > width) {
            slotWidth--;
        }

        while (enemyCount > 1 && spacing > 0 && (slotWidth * enemyCount) + (spacing * (enemyCount - 1)) > width) {
            spacing--;
        }

        if ((slotWidth * enemyCount) + (spacing * Math.max(0, enemyCount - 1)) > width) {
            slotWidth = Math.max(6, width / enemyCount);
            spacing = 0;
        }

        int usedWidth = (slotWidth * enemyCount) + (spacing * Math.max(0, enemyCount - 1));
        int leftPadding = Math.max(0, width - usedWidth);

        Panel enemyRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
        if (leftPadding > 0) {
            enemyRow.addComponent(new EmptySpace(new TerminalSize(leftPadding, 1)));
        }

        int spriteRows = Math.max(1, height - 4);
        int selectedIndex = getSelectedTargetIndex(state.getAliveEnemies().size());
        for (int index = 0; index < state.getAliveEnemies().size(); index++) {
            EnemyViewState enemy = state.getAliveEnemies().get(index);
            enemyRow.addComponent(buildEnemySlot(enemy, index, selectedIndex, slotWidth, height, spriteRows));
            if (index < state.getAliveEnemies().size() - 1 && spacing > 0) {
                enemyRow.addComponent(new EmptySpace(new TerminalSize(spacing, 1)));
            }
        }

        enemyRow.setPreferredSize(new TerminalSize(width, height));
        return enemyRow;
    }

    private int getMaxEnemySpriteWidth(ArenaViewState state) {
        int maxWidth = 10;
        for (EnemyViewState enemy : state.getAliveEnemies()) {
            try {
                AsciiSprite sprite = SpriteCatalog.load("enemy", enemy.getSpriteKey(), "normal");
                maxWidth = Math.max(maxWidth, sprite.getWidth());
            } catch (IOException ignored) {
                maxWidth = Math.max(maxWidth, fallbackEnemyLabel(enemy).length());
            }
        }
        return maxWidth;
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
        if (showInfoButtons) {
            fixedLines++;
        }
        List<String> spriteLines = loadEnemySpriteLines(enemy, contentWidth, spriteRows);
        int topPadding = Math.max(0, slotHeight - fixedLines - spriteLines.size());
        
        Integer targetDmg = enemy.getFloatingDamage();
        boolean renderDmg = targetDmg != null;
        for (int i = 0; i < topPadding; i++) {
            if (renderDmg && i == topPadding - 1) {
                String dmgStr = "  -" + targetDmg;
                Label dmgLabel = new Label(dmgStr);
                dmgLabel.setForegroundColor(TextColor.ANSI.RED);
                content.addComponent(dmgLabel);
            } else {
                content.addComponent(new Label(""));
            }
        }

        content.addComponent(new Button(targetLabel, () -> onTargetSelected(index)));
        if (hasEffects) {
            content.addComponent(new Label(fittedLine(effectLine(enemy), contentWidth)));
        }
        content.addComponent(new Label(fittedLine(hpLine(enemy.getCurrentHp(), enemy.getMaxHp(), contentWidth), contentWidth)));

        if (showInfoButtons) {
            Button infoBtn = createInfoButton("(ℹ)", () -> {
                showInfoDialog(
                    enemy.getName() + " (" + enemy.getType() + ")",
                    enemy.getDescription(),
                    "HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp() + "\n" +
                    "Attack: " + enemy.getAttack() + "\n" +
                    "Defense: " + enemy.getDefense() + "\n" +
                    "Speed: " + enemy.getSpeed()
                );
            });
            content.addComponent(infoBtn);
        }

        for (String spriteLine : spriteLines) {
            content.addComponent(new Label(fittedLine(spriteLine, contentWidth)));
        }

        content.setPreferredSize(new TerminalSize(Math.max(6, slotWidth), Math.max(6, slotHeight)));
        return content;
    }

    private void onTargetSelected(int index) {
        if (lastRenderedState == null) {
            return;
        }

        int enemyCount = lastRenderedState.getAliveEnemies().size();
        if (enemyCount <= 0) {
            return;
        }

        int clampedIndex = Math.max(0, Math.min(index, enemyCount - 1));
        if (clampedIndex == uiSelectedTargetIndex) {
            return;
        }
        uiSelectedTargetIndex = clampedIndex;

        if (window == null || !window.isVisible()) {
            return;
        }

        render(lastRenderedState);
    }

    private int getSelectedTargetIndex(int enemyCount) {
        if (enemyCount <= 0) {
            uiSelectedTargetIndex = 0;
            return 0;
        }
        uiSelectedTargetIndex = Math.max(0, Math.min(uiSelectedTargetIndex, enemyCount - 1));
        return uiSelectedTargetIndex;
    }

    private List<String> loadEnemySpriteLines(EnemyViewState enemy, int maxWidth, int maxRows) {
        try {
            AsciiSprite sprite = SpriteCatalog.load("enemy", enemy.getSpriteKey(), "normal");
            if (sprite.getWidth() > maxWidth) {
                return List.of(fallbackEnemyLabel(enemy));
            }
            List<String> lines = sprite.getLines();
            int rows = Math.min(maxRows, lines.size());
            return lines.subList(0, rows);
        } catch (IOException exception) {
            return List.of(fallbackEnemyLabel(enemy));
        }
    }

    private String fallbackEnemyLabel(EnemyViewState enemy) {
        String type = enemy.getType();
        if (type == null || type.isBlank()) {
            return "[Enemy]";
        }
        String normalized = Character.toUpperCase(type.charAt(0)) + type.substring(1).toLowerCase();
        return "[" + normalized + "]";
    }

    private String effectLine(EnemyViewState enemy) {
        return enemy.getActiveEffects().stream()
            .map(effect -> effect.getIconSymbol() == null || effect.getIconSymbol().isBlank()
                ? effect.getEffectName()
                : effect.getIconSymbol())
            .reduce((left, right) -> left + " " + right)
            .orElse("");
    }

    private String hpLine(int currentHp, int maxHp, int maxWidth) {
        int barWidth = Math.max(4, Math.min(10, maxWidth - 10));
        int filled = Math.max(0, Math.min(barWidth, (int) Math.round((currentHp * barWidth) / (double) maxHp)));
        String bar = "█".repeat(filled) + "░".repeat(Math.max(0, barWidth - filled));
        return bar + " " + currentHp + "/" + maxHp;
    }

    private Component buildUtilitySection(String title, ArenaLayoutCalculator.Rect rect, List<String> contentLines, boolean keepNewestVisible) {
        int innerWidth = Math.max(1, rect.width() - 2);
        int bodyRows = Math.max(1, rect.height() - 2);
        List<String> wrapped = wrapLines(contentLines, innerWidth);

        List<String> visible;
        if (wrapped.size() <= bodyRows) {
            visible = wrapped;
        } else if (keepNewestVisible) {
            visible = wrapped.subList(wrapped.size() - bodyRows, wrapped.size());
        } else {
            visible = wrapped.subList(0, bodyRows);
        }

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(buildTopBorderRow(title, innerWidth));

        for (int i = 0; i < bodyRows; i++) {
            String line = i < visible.size() ? visible.get(i) : "";
            panel.addComponent(buildBodyBorderRow(line, innerWidth));
        }

        panel.addComponent(buildBottomBorderRow(innerWidth));
        panel.setPreferredSize(new TerminalSize(rect.width(), rect.height()));
        return panel;
    }

    private Component buildTopBorderRow(String title, int innerWidth) {
        String topLeft = asciiMode ? "+" : "╭";
        String topRight = asciiMode ? "+" : "╮";
        String horizontal = asciiMode ? "-" : "─";

        String normalized = title == null ? "" : title.trim();
        String titleText = " " + normalized + " ";

        if (innerWidth <= 1) {
            titleText = "";
        } else {
            int maxTitleWidth = Math.max(0, innerWidth - 1);
            if (titleText.length() > maxTitleWidth) {
                titleText = titleText.substring(0, maxTitleWidth);
            }
        }

        int fillWidth = Math.max(0, innerWidth - titleText.length());

        LinearLayout rowLayout = new LinearLayout(Direction.HORIZONTAL);
        rowLayout.setSpacing(0);
        Panel row = new Panel(rowLayout);
        Label left = new Label(topLeft);
        Label titleLabel = new Label(titleText);
        titleLabel.setPreferredSize(new TerminalSize(titleText.length(), 1));
        Label fillLabel = new Label(horizontal.repeat(fillWidth));
        fillLabel.setPreferredSize(new TerminalSize(fillWidth, 1));
        Label right = new Label(topRight);
        row.addComponent(left);
        row.addComponent(titleLabel);
        row.addComponent(fillLabel);
        row.addComponent(right);
        row.setPreferredSize(new TerminalSize(innerWidth + 2, 1));
        return row;
    }

    private Component buildBodyBorderRow(String text, int innerWidth) {
        String vertical = asciiMode ? "|" : "│";
        String content = fittedLine(text == null ? "" : text, innerWidth);

        LinearLayout rowLayout = new LinearLayout(Direction.HORIZONTAL);
        rowLayout.setSpacing(0);
        Panel row = new Panel(rowLayout);
        Label left = new Label(vertical);
        Label center = new Label(padRight(content, innerWidth));
        center.setPreferredSize(new TerminalSize(innerWidth, 1));
        Label right = new Label(vertical);
        row.addComponent(left);
        row.addComponent(center);
        row.addComponent(right);
        row.setPreferredSize(new TerminalSize(innerWidth + 2, 1));
        return row;
    }

    private Component buildBottomBorderRow(int innerWidth) {
        String horizontal = asciiMode ? "-" : "─";
        String bottomLeft = asciiMode ? "+" : "╰";
        String bottomRight = asciiMode ? "+" : "╯";

        LinearLayout rowLayout = new LinearLayout(Direction.HORIZONTAL);
        rowLayout.setSpacing(0);
        Panel row = new Panel(rowLayout);
        Label left = new Label(bottomLeft);
        Label center = new Label(horizontal.repeat(innerWidth));
        center.setPreferredSize(new TerminalSize(innerWidth, 1));
        Label right = new Label(bottomRight);
        row.addComponent(left);
        row.addComponent(center);
        row.addComponent(right);
        row.setPreferredSize(new TerminalSize(innerWidth + 2, 1));
        return row;
    }

    private List<String> wrapLines(List<String> lines, int maxWidth) {
        List<String> wrapped = new ArrayList<>();
        int width = Math.max(1, maxWidth);
        if (lines == null || lines.isEmpty()) {
            return wrapped;
        }

        for (String line : lines) {
            String value = line == null ? "" : line;
            if (value.isEmpty()) {
                wrapped.add("");
                continue;
            }

            String remaining = value;
            while (!remaining.isEmpty()) {
                if (remaining.length() <= width) {
                    wrapped.add(remaining);
                    break;
                }

                int breakAt = remaining.lastIndexOf(' ', width);
                if (breakAt <= 0) {
                    breakAt = width;
                    wrapped.add(remaining.substring(0, breakAt));
                    remaining = remaining.substring(breakAt);
                } else {
                    wrapped.add(remaining.substring(0, breakAt));
                    remaining = remaining.substring(breakAt + 1);
                }

                while (!remaining.isEmpty() && remaining.charAt(0) == ' ') {
                    remaining = remaining.substring(1);
                }
            }
        }
        return wrapped;
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
            return loadSprite("item", "no_item", maxWidth, maxRows);
        }
        String firstItem = state.getAvailableItems().stream()
            .filter(name -> name != null && !name.isBlank() && !"None".equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);

        if (firstItem == null) {
            return loadSprite("item", "no_item", maxWidth, maxRows);
        }
        String itemName = firstItem.toLowerCase().replace(' ', '_');
        return loadSprite("item", itemName, maxWidth, maxRows);
    }

    private List<String> loadInventorySlotSprite(String itemName, int maxWidth, int maxRows) {
        if (itemName == null || itemName.isBlank() || "None".equalsIgnoreCase(itemName)) {
            return loadSprite("item", "no_item", maxWidth, maxRows);
        }
        return loadSprite("item", itemName.toLowerCase().replace(' ', '_'), maxWidth, maxRows);
    }

    private String getItemNameAt(ArenaViewState state, int index) {
        if (state.getAvailableItems() == null || index < 0 || index >= state.getAvailableItems().size()) {
            return "None";
        }
        String itemName = state.getAvailableItems().get(index);
        return (itemName == null || itemName.isBlank()) ? "None" : itemName;
    }

    private String getItemDescriptionAt(ArenaViewState state, int index) {
        if (state.getAvailableItemDescriptions() == null
            || index < 0
            || index >= state.getAvailableItemDescriptions().size()) {
            return "No item selected in this slot.";
        }
        String description = state.getAvailableItemDescriptions().get(index);
        return (description == null || description.isBlank())
            ? "No item selected in this slot."
            : description;
    }

    private String getSkillDisplayName(ArenaViewState state) {
        if (state == null || state.getPlayerState() == null || state.getPlayerState().getType() == null) {
            return "Special Skill";
        }
        return switch (state.getPlayerState().getType()) {
            case "Wizard" -> "Arcane Blast";
            case "Warrior" -> "Shield Bash";
            default -> "Special Skill";
        };
    }

    private List<String> buildInventoryDisplayLines(ArenaViewState state, int width) {
        List<String> lines = new ArrayList<>();
        String slot1 = state.getAvailableItems().size() > 0 ? state.getAvailableItems().get(0) : "None";
        String slot2 = state.getAvailableItems().size() > 1 ? state.getAvailableItems().get(1) : "None";
        lines.add(fittedLine("Item 1: " + slot1, width));
        lines.add(fittedLine("Item 2: " + slot2, width));
        return lines;
    }

    private void showItemSelectionDialog(ArenaViewState state, int targetIdx) {
        if (gui == null || callbacks == null || state == null || state.getAvailableItems().isEmpty()) {
            return;
        }

        BasicWindow itemWindow = new BasicWindow();
        itemWindow.setHints(java.util.Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING));
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label("Choose item to use"));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        for (int i = 0; i < state.getAvailableItems().size(); i++) {
            String itemName = state.getAvailableItems().get(i);
            int itemIndex = i;
            panel.addComponent(new Button(itemName, () -> {
                itemWindow.close();
                callbacks.onUseItem(itemIndex, targetIdx);
            }));
        }

        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        panel.addComponent(new Button("Cancel", itemWindow::close));
        itemWindow.setComponent(panel);
        gui.addWindowAndWait(itemWindow);
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
            return SpriteCatalog.loadArenaStrip("forest", maxWidth).clipRight(maxWidth, rows).getLines();
        } catch (IOException exception) {
            return List.of("_".repeat(Math.max(1, maxWidth)));
        }
    }

    private List<String> loadCloudLines(int maxWidth, int rows) {
        if (rows <= 0) {
            return List.of();
        }
        try {
            List<String> lines = SpriteCatalog.loadCloudStrip(maxWidth).clipRight(maxWidth, rows).getLines();
            if (lines.size() >= rows) {
                return lines;
            }

            List<String> padded = new ArrayList<>(lines);
            while (padded.size() < rows) {
                padded.add(" ".repeat(Math.max(1, maxWidth)));
            }
            return padded;
        } catch (IOException exception) {
            return blankRows(maxWidth, rows);
        }
    }

    private List<String> blankRows(int width, int rows) {
        List<String> fallback = new ArrayList<>();
        String blank = " ".repeat(Math.max(1, width));
        for (int i = 0; i < rows; i++) {
            fallback.add(blank);
        }
        return fallback;
    }

    private List<String> loadSprite(String category, String name, int maxWidth, int maxRows) {
        try {
            return SpriteCatalog.load(category, name, "normal").clipRight(maxWidth, maxRows).getLines();
        } catch (IOException exception) {
            return List.of("[" + name + "]");
        }
    }
}
