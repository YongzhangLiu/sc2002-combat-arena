package arena.ui.model;

/**
 * Single status effect icon/timer display for UI rendering.
 * Immutable snapshot of effect state.
 */
public class StatusIconViewState {
    private final String effectName;
    private final int remainingTurns; // 0 if permanent or already active
    private final String iconSymbol; // e.g., "S" for stunned, "D" for defend, etc.

    public StatusIconViewState(String effectName, int remainingTurns, String iconSymbol) {
        this.effectName = effectName;
        this.remainingTurns = Math.max(0, remainingTurns);
        this.iconSymbol = iconSymbol;
    }

    public String getEffectName() {
        return effectName;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    @Override
    public String toString() {
        if (remainingTurns > 0) {
            return effectName + "(" + remainingTurns + ")";
        }
        return effectName;
    }
}
