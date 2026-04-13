package arena.strategy;

import java.util.List;
import arena.model.combatant.Combatant;
import arena.model.combatant.Player;

public interface TurnOrderStrategy {
    List<Combatant> buildTurnOrder(List<Player> player, List<Combatant> opponents);
}