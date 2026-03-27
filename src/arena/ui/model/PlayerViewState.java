package arena.ui.model;

import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the player character for UI rendering.
 */
public class PlayerViewState {
    private final String name;
    private final String type; // e.g., "Warrior", "Mage", "Rogue"
    private final int currentHp;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final String spriteKey; // identifier for sprite lookup
    private final List<StatusIconViewState> activeEffects;

    public PlayerViewState(String name, String type, int currentHp, int maxHp,
                          int attack, int defense, int speed, String spriteKey,
                          List<StatusIconViewState> activeEffects) {
        this.name = name;
        this.type = type;
        this.currentHp = Math.max(0, currentHp);
        this.maxHp = Math.max(1, maxHp);
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spriteKey = spriteKey;
        this.activeEffects = activeEffects != null
            ? Collections.unmodifiableList(activeEffects)
            : Collections.emptyList();
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

    public List<StatusIconViewState> getActiveEffects() {
        return activeEffects;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public int getHpPercent() {
        return (int) Math.round((100.0 * currentHp) / maxHp);
    }
}
