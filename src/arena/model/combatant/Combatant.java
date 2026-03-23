package arena.model.combatant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import arena.model.effect.StatusEffect;

public abstract class Combatant {
    private final String name;
    private int hp;
    private final int maxHp;
    private int attack;
    private final int baseDefense;
    private final int speed;
    private final List<StatusEffect> activeEffects;

    protected Combatant(String name, int maxHp, int attack, int defense, int speed) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.baseDefense = defense;
        this.speed = speed;
        this.activeEffects = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        int modifiedDefense = baseDefense;
        for (StatusEffect effect : activeEffects) {
            modifiedDefense = effect.modifyDefense(modifiedDefense);
        }
        return modifiedDefense;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void heal(int amount) {
        if (amount <= 0 || !isAlive()) {
            return;
        }
        hp = Math.min(maxHp, hp + amount);
    }

    public int takeDamage(int amount) {
        return takeDamage(amount, null);
    }

    public int takeDamage(int amount, Combatant attacker) {
        if (amount <= 0 || !isAlive()) {
            return 0;
        }

        int adjusted = amount;
        for (StatusEffect effect : activeEffects) {
            adjusted = effect.modifyIncomingDamage(adjusted, attacker, this);
        }
        adjusted = Math.max(0, adjusted);

        hp = Math.max(0, hp - adjusted);
        return adjusted;
    }

    public boolean beginTurn() {
        if (!isAlive()) {
            return false;
        }

        boolean canAct = true;
        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            if (effect.onTurnStart(this)) {
                canAct = false;
            }
            if (effect.isExpired()) {
                effect.onExpire(this);
                iterator.remove();
            }
        }
        return canAct;
    }

    public void addStatusEffect(StatusEffect effect) {
        if (effect == null) {
            return;
        }
        activeEffects.add(effect);
        effect.onApply(this);
    }

    public List<StatusEffect> getActiveEffects() {
        return Collections.unmodifiableList(activeEffects);
    }

    public boolean hasEffect(Class<? extends StatusEffect> effectType) {
        for (StatusEffect effect : activeEffects) {
            if (effectType.isInstance(effect)) {
                return true;
            }
        }
        return false;
    }

    protected void increaseAttack(int amount) {
        if (amount > 0) {
            attack += amount;
        }
    }
}
