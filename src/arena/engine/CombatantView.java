package arena.engine;

import java.util.List;

/**
 * Read-only view of a combatant for UI rendering.
 * Engine populates this from domain Combatant; UI never mutates it.
 */
public class CombatantView {
    private final String name;
    private final int currentHp;
    private final int maxHp;
    private final List<String> activeEffects; // e.g., ["Stunned", "DefendBuff"]
    private final boolean isAlive;

    public CombatantView(String name, int currentHp, int maxHp,
                         List<String> activeEffects, boolean isAlive) {
        this.name = name;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.activeEffects = activeEffects;
        this.isAlive = isAlive;
    }

    public String getName() {
        return name;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public List<String> getActiveEffects() {
        return activeEffects;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public double getHpPercent() {
        return maxHp > 0 ? (double) currentHp / maxHp : 0;
    }
}
