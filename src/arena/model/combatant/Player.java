package arena.model.combatant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arena.model.item.Item;

public abstract class Player extends Combatant {
    private static final int SPECIAL_SKILL_COOLDOWN = 3;

    private int specialSkillCooldown;
    private final List<Item> inventory;

    protected Player(String name, int maxHp, int attack, int defense, int speed) {
        super(name, maxHp, attack, defense, speed);
        this.inventory = new ArrayList<>();
        this.specialSkillCooldown = 0;
    }

    public int getSpecialSkillCooldown() {
        return specialSkillCooldown;
    }

    public boolean canUseSpecialSkill() {
        return specialSkillCooldown == 0;
    }

    public void useSpecialSkill(List<Enemy> enemies, Enemy target) {
        if (!canUseSpecialSkill()) {
            throw new IllegalStateException("Special skill is on cooldown.");
        }
        performSpecialSkill(enemies, target);
        specialSkillCooldown = SPECIAL_SKILL_COOLDOWN;
    }

    public void triggerSpecialSkillWithoutCooldown(List<Enemy> enemies, Enemy target) {
        performSpecialSkill(enemies, target);
    }

    public void decrementCooldownAfterTurn() {
        if (specialSkillCooldown > 0) {
            specialSkillCooldown--;
        }
    }

    protected abstract void performSpecialSkill(List<Enemy> enemies, Enemy target);

    public void addItem(Item item) {
        if (item != null) {
            inventory.add(item);
        }
    }

    public boolean removeItem(Item item) {
        return inventory.remove(item);
    }

    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }
}
