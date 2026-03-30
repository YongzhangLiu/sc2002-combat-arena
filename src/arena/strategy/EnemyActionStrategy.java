package arena.strategy;

import java.util.List;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public interface EnemyActionStrategy {
    void execute(Enemy enemy, List<Player> players);
}