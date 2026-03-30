package arena.strategy;

import java.util.List;
import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public interface TurnOrderStrategy {
    List<Combatant> buildTurnOrder(Player player, List<Enemy> enemies);
}