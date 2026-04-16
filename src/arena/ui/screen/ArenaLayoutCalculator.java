package arena.ui.screen;

import com.googlecode.lanterna.TerminalSize;

/**
 * Computes arena panel bounds from terminal size.
 * Recalculate on every render/update to support resize.
 */
public class ArenaLayoutCalculator {
    private static final int MIN_PANEL_WIDTH = 14;
    private static final int MAX_INVENTORY_WIDTH = 40;
    private static final int MAX_STATUS_WIDTH = 26;
    private static final int MIN_INFO_WIDTH = 12;
    private static final int UTILITY_HEIGHT = 12;
    private static final int UTILITY_GAP = 1;

    public LayoutBounds calculate(TerminalSize size) {
        int width = Math.max(40, size.getColumns());
        int height = Math.max(20, size.getRows());

        boolean compactMode = width < 90 || height < 30;

        int utilityHeight = Math.min(UTILITY_HEIGHT, Math.max(6, height - 10));
        int actionBarHeight = compactMode ? 3 : 4;
        int arenaHeight = Math.max(6, height - utilityHeight - actionBarHeight);

        int totalGaps = UTILITY_GAP * 2;
        int availableUtilityWidth = Math.max(MIN_PANEL_WIDTH * 3, width - totalGaps);

        // Keep logs compact and give inventory more horizontal room for two item slots.
        int inventoryWidth = Math.min(MAX_INVENTORY_WIDTH, Math.max(MIN_PANEL_WIDTH, (int) Math.round(availableUtilityWidth * 0.45)));
        int statusWidth = Math.min(MAX_STATUS_WIDTH, Math.max(MIN_PANEL_WIDTH, (int) Math.round(availableUtilityWidth * 0.22)));
        int infoWidth = availableUtilityWidth - inventoryWidth - statusWidth;

        if (infoWidth < MIN_INFO_WIDTH) {
            int required = MIN_INFO_WIDTH - infoWidth;

            int reduceFromInventory = Math.min(required, Math.max(0, inventoryWidth - MIN_PANEL_WIDTH));
            inventoryWidth -= reduceFromInventory;
            required -= reduceFromInventory;

            int reduceFromStatus = Math.min(required, Math.max(0, statusWidth - MIN_PANEL_WIDTH));
            statusWidth -= reduceFromStatus;

            infoWidth = availableUtilityWidth - inventoryWidth - statusWidth;
        }

        Rect inventoryPanel = new Rect(0, 0, inventoryWidth, utilityHeight);
        Rect infoPanel = new Rect(inventoryWidth + UTILITY_GAP, 0, infoWidth, utilityHeight);
        Rect statusPanel = new Rect(inventoryWidth + UTILITY_GAP + infoWidth + UTILITY_GAP, 0, statusWidth, utilityHeight);

        Rect arenaPanel = new Rect(0, utilityHeight, width, arenaHeight);
        Rect actionBar = new Rect(0, utilityHeight + arenaHeight, width, actionBarHeight);

        return new LayoutBounds(
            new Rect(0, 0, width, height),
            inventoryPanel,
            infoPanel,
            statusPanel,
            arenaPanel,
            actionBar,
            compactMode
        );
    }

    /**
     * Width allocation for each alive enemy slot.
     */
    public int getEnemySlotWidth(int enemyPanelWidth, int aliveEnemyCount) {
        if (enemyPanelWidth <= 0 || aliveEnemyCount <= 0) {
            return 0;
        }
        return Math.max(1, enemyPanelWidth / aliveEnemyCount);
    }

    public record Rect(int column, int row, int width, int height) {
        public int right() {
            return column + width;
        }

        public int bottom() {
            return row + height;
        }
    }

    public record LayoutBounds(
        Rect canvas,
        Rect inventoryPanel,
        Rect infoPanel,
        Rect statusPanel,
        Rect arenaPanel,
        Rect actionBar,
        boolean compactMode
    ) {
    }
}
