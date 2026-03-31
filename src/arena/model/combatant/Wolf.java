package arena.model.combatant;

import arena.strategy.BasicEnemyActionStrategy;

public class Wolf extends Enemy {
    public Wolf(String name) {
        super(name, 40, 45, 5, 35, new BasicEnemyActionStrategy());
    }
}
