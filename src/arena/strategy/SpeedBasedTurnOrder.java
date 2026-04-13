package arena.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import arena.model.combatant.Combatant;
import arena.model.combatant.Player;

public class SpeedBasedTurnOrder implements TurnOrderStrategy {

    @Override
    public List<Combatant> buildTurnOrder(List<Player> players, List<Combatant> opponents) {
        List<Combatant> order = new ArrayList<>();
        for (Player player : players) {
            if (player != null && player.isAlive()) {
                order.add(player);
            }
        }

        for (Combatant opponent : opponents) {
            if (opponent != null && opponent.isAlive()) {
                order.add(opponent);
            }
        }

        // Strict speed-based ordering across all combatants.
        order.sort(Comparator.comparingInt(Combatant::getSpeed).reversed());
        return order;
    }
}