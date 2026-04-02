package arena.model.combatant;

import java.util.List;

import arena.model.effect.StunEffect;

public class Warrior extends Player {
    public Warrior(String name) {
        super(name, 260, 40, 20, 30);
        setDescription("Tanky and resilient fighter.");
    }

    @Override
    protected void performSpecialSkill(List<Enemy> enemies, Enemy target) {
        if (target == null || !target.isAlive()) {
            throw new IllegalArgumentException("Warrior special skill needs a living enemy target.");
        }
        int damage = Math.max(0, getAttack() - target.getDefense());
        target.takeDamage(damage, this);
        target.addStatusEffect(new StunEffect());
    }
}
