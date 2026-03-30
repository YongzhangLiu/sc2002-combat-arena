package arena.engine;

import java.util.ArrayList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;


public class GameState {
    private static int currentRound;
    private static Player player1;
    private static List<List<Enemy>> enemies = new ArrayList<>();
    private static List<Combatant> turnOrder = new ArrayList<Combatant>();
    private static boolean updateStatus; //flag to show that gameState has been changed

    /*
    public static void gameStart(Player player, List<List<Enemy>> enemyList){     //Call when battle starts
        currentRound = 1;
        player1 = player;
        enemies = enemyList;
        newTurnOrder();
        //createTurnOrder^^^ calls function from strategy to create an arraylist of combatants ordered by turnorder 
    }
    */
    
    public static void setCurrentRound(int newRound){
        currentRound = newRound;
    }

    public static void setPlayer(Player player){
        player1 = player;
    }

    public static void setEnemies(List<List<Enemy>> enemyList){
        enemies = enemyList;
    }

    public static void setCurrentWave(List<Enemy> waveList){
        enemies.set(0, waveList);
    }

    public static void setTurnOrder(List<Combatant> newOrder){
        turnOrder = newOrder;
    }

    public static void newTurnOrder(){
        turnOrder = createTurnOrder(player1, enemies.get(0));
    }

    public static boolean getUpdateStatus(){
        return updateStatus;
    }

    public static Player getPlayer(){
        return player1;
    }

    public static int getCurrentRound(){
        return currentRound;
    }

    public static Combatant getCurrentAttacker(){
        return turnOrder.get(0);
    }

    public static int getEnemyCount(List<List<Enemy>> enemies){
        return enemies.get(0).size();
    }

    public static List<Enemy> getCurrentWave(){
        return enemies.get(0);
    }

    public static List<Combatant> getTurnOrder(){
        return turnOrder;
    }

    public static int checkEndCondition(){ //if enemies are dead, returns 1
        if (enemies.isEmpty()) {          //if player is dead, returns 2
            return 1;                      //if no condition fulfilled, returns 0, game continues
        }else if (!player1.isAlive()) {
            return 2;
        }else return 0;
    }

}
