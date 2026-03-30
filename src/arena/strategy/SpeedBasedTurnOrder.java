package arena.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public class SpeedBasedTurnOrder implements TurnOrderStrategy {

    @Override
    public List<Combatant> buildTurnOrder(Player player, List<Enemy> enemies) {
        List<Combatant> order = new ArrayList<>();
        order.add(player);
        order.addAll(enemies);

        //Sort descending by speed —> highest SPD goes first
        order.sort(Comparator.comparingInt(Combatant::getSpeed).reversed());

        return order;
    }
}