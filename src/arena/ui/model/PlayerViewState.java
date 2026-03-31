package arena.ui.model;

import java.util.Collections;
import java.util.List;

public class PlayerViewState {
    private final String name;
    private final String type;
    private final int currentHp;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final String spriteKey;
    private final List<StatusIconViewState> activeEffects;
    private final int specialSkillCooldown;
    private final boolean isSpecialSkillAvailable;
    private final boolean hasItems;
    private final Integer floatingDamage;

    public PlayerViewState(String name, String type, int currentHp, int maxHp,
                          int attack, int defense, int speed, String spriteKey,
                          int specialSkillCooldown, boolean isSpecialSkillAvailable,
                          boolean hasItems,
                          List<StatusIconViewState> activeEffects) {
        this(name, type, currentHp, maxHp, attack, defense, speed, spriteKey, specialSkillCooldown, isSpecialSkillAvailable, hasItems, activeEffects, null);
    }

    public PlayerViewState(String name, String type, int currentHp, int maxHp,
                          int attack, int defense, int speed, String spriteKey,
                          int specialSkillCooldown, boolean isSpecialSkillAvailable,
                          boolean hasItems,
                          List<StatusIconViewState> activeEffects, Integer floatingDamage) {
        this.name = name;
        this.type = type;
        this.currentHp = Math.max(0, currentHp);
        this.maxHp = Math.max(1, maxHp);
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spriteKey = spriteKey;
        this.specialSkillCooldown = specialSkillCooldown;
        this.isSpecialSkillAvailable = isSpecialSkillAvailable;
        this.hasItems = hasItems;
        this.activeEffects = activeEffects != null ? Collections.unmodifiableList(activeEffects) : Collections.emptyList();
        this.floatingDamage = floatingDamage;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public String getSpriteKey() { return spriteKey; }
    public List<StatusIconViewState> getActiveEffects() { return activeEffects; }
    public int getSpecialSkillCooldown() { return specialSkillCooldown; }
    public boolean isSpecialSkillAvailable() { return isSpecialSkillAvailable; }
    public boolean hasItems() { return hasItems; }
    public Integer getFloatingDamage() { return floatingDamage; }
}
