package arena.engine;

import java.util.ArrayList;
import java.util.List;

import arena.model.action.Action;
import arena.model.action.BasicAttack;
import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class BattleEngine{
    PlayerAction playerAction = new PlayerAction();

    public void endRound(){
        int currentRound = GameState.getCurrentRound();
        GameState.setCurrentRound(currentRound+1);
    }

    // returns 0 if game has not ended. If game over condition is met it returns 1 for win and 2 for loss
    public int executeTurn(int playerChoice, int target, Item item){
        List<Combatant> turnOrder = GameState.getTurnOrder();
        Player player1 = GameState.getPlayer();
        List<Enemy> currentWave = GameState.getCurrentWave();
        Enemy targetEnemy = currentWave.get(target);
        Combatant attacker = turnOrder.get(0);

        if (attacker.beginTurn()) {
            if (attacker instanceof Enemy){
                Action bAttack = new BasicAttack();
                bAttack.execute(attacker, player1, currentWave);
            }else{
                switch (playerChoice) {
                    case 1: //basic attack
                        playerAction.bAttack(player1, targetEnemy);
                        break;

                    case 2:  //defend
                        playerAction.defend(player1);
                        break;

                    case 3:  //item
                        playerAction.consumeItem(player1, targetEnemy, currentWave, item);
                        break;

                    case 4:  //special
                        playerAction.specialSkill(player1, targetEnemy, currentWave);
                        break;
                
                    default:
                        break;
                }
                if (!targetEnemy.isAlive()) {
                    currentWave.remove(target);
                    GameState.setCurrentWave(currentWave);
                }
            }
        }
        turnOrder.remove(0);
        GameState.setTurnOrder(turnOrder); 
/*im not sure if i need to set the gamestate turnorder array
 or if it automatically changes when i change it in this function */
        return GameState.checkEndCondition();
    }

}
