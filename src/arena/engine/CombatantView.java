package arena.engine;

import java.util.List;

/**
 * Read-only view of a combatant for UI rendering.
 * Engine populates this from domain Combatant; UI never mutates it.
 */
public class CombatantView {
    private final String name;
    private final String type;
    private final int currentHp;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final int specialCooldownRemaining;
    private final List<String> activeEffects; // e.g., ["Stunned", "DefendBuff"]
    private final boolean isAlive;

    public CombatantView(String name, int currentHp, int maxHp,
                         List<String> activeEffects, boolean isAlive) {
        this(name, "Unknown", currentHp, maxHp, 0, 0, 0, 0, activeEffects, isAlive);
    }

    public CombatantView(String name, String type, int currentHp, int maxHp,
                         int attack, int defense, int speed, int specialCooldownRemaining,
                         List<String> activeEffects, boolean isAlive) {
        this.name = name;
        this.type = type;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.specialCooldownRemaining = specialCooldownRemaining;
        this.activeEffects = activeEffects;
        this.isAlive = isAlive;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSpecialCooldownRemaining() {
        return specialCooldownRemaining;
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
