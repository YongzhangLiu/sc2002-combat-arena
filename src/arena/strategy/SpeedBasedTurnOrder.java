package arena.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public class SpeedBasedTurnOrder implements TurnOrderStrategy {

    @Override
    public List<Combatant> buildTurnOrder(List<Player> players, List<Enemy> enemies) {
        List<Combatant> order = new ArrayList<>();
        for(Player player : players){
            order.add(player); // Player always goes first!
        }
        
        List<Combatant> enemyOrder = new ArrayList<>(enemies);
        //Sort enemies descending by speed
        enemyOrder.sort(Comparator.comparingInt(Combatant::getSpeed).reversed());
        
        order.addAll(enemyOrder);
        return order;
    }
}