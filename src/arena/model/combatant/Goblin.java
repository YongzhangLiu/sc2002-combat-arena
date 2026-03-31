package arena.model.combatant;

import arena.strategy.BasicEnemyActionStrategy;

public class Goblin extends Enemy {
    public Goblin(String name) {
        super(name, 55, 35, 15, 25, new BasicEnemyActionStrategy());
    }
}
