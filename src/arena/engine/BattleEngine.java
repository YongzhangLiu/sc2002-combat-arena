package arena.engine;

import java.util.ArrayList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class BattleEngine{
    PlayerAction playerAction = new PlayerAction();

    public void endRound(){
        int currentRound = GameState.getCurrentRound();
        GameState.setCurrentRound(currentRound+1);
        GameState.newTurnOrder(); // Ensure turn order is rebuilt for the next round
    }

    /**
     * Executes the Player's chosen action against the target.
     * Returns the 0, 1 (win), 2 (loss) status.
     * Use advanceTurnQueue() to let enemies move after player.
     */
    public int executePlayerTurn(int playerChoice, int target, Item item){
        Player player = GameState.getPlayer();
        List<Enemy> currentWave = GameState.getCurrentWave();
        
        if (target < 0 || target >= currentWave.size()) {
            return GameState.checkEndCondition();
        }
        
        Enemy targetEnemy = currentWave.get(target);

        if (player.beginTurn()) {
            switch (playerChoice) {
                case 1: //basic attack
                    GameState.addLog(player.getName() + " performed an attack on " + targetEnemy.getName() + "!");
                    playerAction.bAttack(player, targetEnemy);
                    break;
                case 2: //defend
                    GameState.addLog(player.getName() + " is defending!");
                    playerAction.defend(player);
                    break;
                case 3: //item
                    String itemName = item != null ? item.getName() : "an unknown item";
                    GameState.addLog(player.getName() + " used " + itemName + "!");
                    playerAction.consumeItem(player, targetEnemy, currentWave, item);
                    break;
                case 4: //special
                    // CD logic is currently handled in UI, this is unreachable if skill is in CD. 
                    String skillName = player instanceof arena.model.combatant.Warrior ? "Shield Bash" : "Arcane Blast";
                    GameState.addLog(player.getName() + " used " + skillName + "!");
                    playerAction.specialSkill(player, targetEnemy, currentWave);
                    break;
                default:
                    break;
            }
            if (!targetEnemy.isAlive()) {
                GameState.addLog(targetEnemy.getName() + " was defeated!");
                currentWave.remove(targetEnemy);
                GameState.setCurrentWave(currentWave);
                // Also remove from turn order so dead enemies don't attack
                GameState.getTurnOrder().remove(targetEnemy);
            }
        }
        
        List<Combatant> turnOrder = GameState.getTurnOrder();
        if (!turnOrder.isEmpty() && turnOrder.get(0) instanceof Player) {
            turnOrder.remove(0); // Pop the player off the queue since they just went
        }
        
        return GameState.checkEndCondition();
    }

    /**
     * Fast-forwards the engine through enemy turns until it's the Player's turn again, 
     * or until the wave/game ends.
     * Use this strictly for letting the engine process automatically without UI input.
     * Returns 0 for continue, 1 for win, 2 for loss.
     */
    public int advanceTurnQueue() {
        int endCondition = GameState.checkEndCondition();
        if (endCondition != 0) return endCondition;

        List<Combatant> turnOrder = GameState.getTurnOrder();
        Player player = GameState.getPlayer();

        while (!turnOrder.isEmpty()) {
            Combatant currentAttacker = turnOrder.get(0);
            
            // If it's a player's turn, we must PAUSE and return to the UI for input
            if (currentAttacker instanceof Player) {
                return 0; // Game continues, waiting for UI
            }

            // It's an Enemy. Process their AI Strategy automatically.
            if (currentAttacker instanceof Enemy enemy) {
                if (enemy.isAlive() && enemy.beginTurn()) {
                    if (enemy.getStrategy() != null) {
                        GameState.addLog(enemy.getName() + " performed an attack on " + player.getName() + "!");
                        List<Player> targetList = new ArrayList<>();
                        targetList.add(player);
                        enemy.getStrategy().execute(enemy, targetList);
                    }
                }
            }
            
            // Pop the enemy off the queue
            turnOrder.remove(0);
            
            // Check if player died
            if (!player.isAlive()) {
                GameState.addLog(player.getName() + " was killed by " + currentAttacker.getName() + "!");
            }
            
            // Ensure we check end conds periodically (ex: enemy killed player)
            endCondition = GameState.checkEndCondition();
            if (endCondition != 0) return endCondition;
        }

        // If queue is empty, the round is over.
        endRound();
        
        // After starting a new round, we still need to potentially run enemies that are faster than the player
        // Recursing allows that safely!
        return advanceTurnQueue();
    }
}
