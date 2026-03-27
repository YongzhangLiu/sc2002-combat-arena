package arena.ui.screen;

import com.googlecode.lanterna.TerminalSize;

/**
 * Computes arena panel bounds from terminal size.
 * Recalculate on every render/update to support resize.
 */
public class ArenaLayoutCalculator {
    private static final int MIN_PANEL_WIDTH = 14;
    private static final int INVENTORY_WIDTH = 25;
    private static final int STATUS_WIDTH = 25;
    private static final int UTILITY_HEIGHT = 10;

    public LayoutBounds calculate(TerminalSize size) {
        int width = Math.max(40, size.getColumns());
        int height = Math.max(20, size.getRows());

        boolean compactMode = width < 90 || height < 30;

        int utilityHeight = Math.min(UTILITY_HEIGHT, Math.max(6, height - 10));
        int actionBarHeight = compactMode ? 3 : 4;
        int arenaHeight = Math.max(6, height - utilityHeight - actionBarHeight);

        int inventoryWidth = Math.min(INVENTORY_WIDTH, width);
        int statusWidth = Math.min(STATUS_WIDTH, Math.max(0, width - inventoryWidth));
        int infoWidth = width - inventoryWidth - statusWidth;

        if (infoWidth < MIN_PANEL_WIDTH) {
            int required = MIN_PANEL_WIDTH - infoWidth;

            int reduceFromInventory = Math.min(required, Math.max(0, inventoryWidth - MIN_PANEL_WIDTH));
            inventoryWidth -= reduceFromInventory;
            required -= reduceFromInventory;

            int reduceFromStatus = Math.min(required, Math.max(0, statusWidth - MIN_PANEL_WIDTH));
            statusWidth -= reduceFromStatus;

            infoWidth = width - inventoryWidth - statusWidth;
        }

        Rect inventoryPanel = new Rect(0, 0, inventoryWidth, utilityHeight);
        Rect infoPanel = new Rect(inventoryWidth, 0, infoWidth, utilityHeight);
        Rect statusPanel = new Rect(inventoryWidth + infoWidth, 0, statusWidth, utilityHeight);

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
