package arena.model.combatant;

import java.util.List;

public class Wizard extends Player {
    public Wizard(String name) {
        super(name, 200, 50, 10, 20);
        setDescription("A mysterious spellcaster with a nice looking wand.");
    }

    @Override
    protected void performSpecialSkill(List<Combatant> enemies, Combatant target) {
        if (enemies == null) {
            throw new IllegalArgumentException("Enemy list cannot be null.");
        }

        for (Combatant enemy : enemies) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            int damage = Math.max(0, getAttack() - enemy.getDefense());
            enemy.takeDamage(damage, this);
            if (!enemy.isAlive()) {
                // Arcane Blast grants +10 attack per kill, immediately.
                increaseAttack(10);
            }
        }
    }
}
