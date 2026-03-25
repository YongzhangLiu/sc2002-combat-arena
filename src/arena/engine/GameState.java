package arena.engine;

import java.util.ArrayList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;


public class GameState {
    protected int currentRound = 1;

    public int getCurrentRound(){
        return currentRound;
    }

    public void endRound(){
        currentRound++;
    }

    public Combatant currentTurn(List<Combatant> sortedCombatants){ //input turnorder
        return sortedCombatants.get(0);
    }

    public int enemyCount(List<List<Enemy>> enemies){
        return enemies.get(0).size();
    }
    
    public ArrayList<Combatant> getTurnOrder(List<List<Enemy>> enemies, Player player1){
        return TurnOrder(enemies.get(0), player1);
    }
}
