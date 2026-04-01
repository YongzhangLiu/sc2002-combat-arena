package arena.ui.model;

import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of a single enemy for UI rendering.
 * Includes targeting index and alive status for layout calculations.
 */
public class EnemyViewState {
    private final int targetIndex; // position in enemy list (0-based)
    private final String name;
    private final String type; // e.g., "Goblin", "Orc", "Dragon"
    private final int currentHp;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final String spriteKey; // identifier for sprite lookup
    private final boolean isAlive;
    private final List<StatusIconViewState> activeEffects;
    private final Integer floatingDamage;

    public EnemyViewState(int targetIndex, String name, String type,
                         int currentHp, int maxHp,
                         int attack, int defense, int speed,
                         String spriteKey, boolean isAlive,
                         List<StatusIconViewState> activeEffects) {
        this(targetIndex, name, type, currentHp, maxHp, attack, defense, speed, spriteKey, isAlive, activeEffects, null);
    }

    public EnemyViewState(int targetIndex, String name, String type,
                         int currentHp, int maxHp,
                         int attack, int defense, int speed,
                         String spriteKey, boolean isAlive,
                         List<StatusIconViewState> activeEffects,
                         Integer floatingDamage) {
        this.targetIndex = targetIndex;
        this.name = name;
        this.type = type;
        this.currentHp = Math.max(0, currentHp);
        this.maxHp = Math.max(1, maxHp);
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spriteKey = spriteKey;
        this.isAlive = isAlive && (currentHp > 0);
        this.activeEffects = activeEffects != null
            ? Collections.unmodifiableList(activeEffects)
            : Collections.emptyList();
        this.floatingDamage = floatingDamage;
    }

    public int getTargetIndex() {
        return targetIndex;
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

    public String getSpriteKey() {
        return spriteKey;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public List<StatusIconViewState> getActiveEffects() {
        return activeEffects;
    }

    public Integer getFloatingDamage() {
        return floatingDamage;
    }

    public int getHpPercent() {
        return (int) Math.round((100.0 * currentHp) / maxHp);
    }
}
