package arena.strategy;

import java.util.List;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public class BasicEnemyActionStrategy implements EnemyActionStrategy {

    @Override
    public void execute(Enemy enemy, List<Player> players) {
        for (Player p : players) {
            if (p.isAlive()) {
                int damage = Math.max(0, enemy.getAttack() - p.getDefense());
                p.takeDamage(damage, enemy);
                break; //only attack one player
            }
        }
    }
}
