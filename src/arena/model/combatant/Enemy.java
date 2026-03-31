package arena.model.combatant;

import arena.strategy.EnemyActionStrategy;

public abstract class Enemy extends Combatant {
    private EnemyActionStrategy strategy;

    protected Enemy(String name, int maxHp, int attack, int defense, int speed, EnemyActionStrategy strategy) {
        super(name, maxHp, attack, defense, speed);
        this.strategy = strategy;
    }

    public EnemyActionStrategy getStrategy() {
        return strategy;
    }
}
