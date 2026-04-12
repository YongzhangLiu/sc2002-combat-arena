package arena.engine;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;


public class GameState {
    private static int currentRound;
    private static List<Player> players = new ArrayList<>();
    private static List<List<Enemy>> enemies = new ArrayList<>();
    private static List<Combatant> turnOrder = new ArrayList<Combatant>();
    private static boolean updateStatus; //flag to show that gameState has been changed

    public static final int MAX_LOG_LINES = 4;
    private static Deque<String> combatLog = new LinkedList<>();

    public static void addLog(String message) {
        if (message != null && !message.isEmpty()) {
            combatLog.addLast(message);
        }
        while (combatLog.size() > MAX_LOG_LINES) {
            combatLog.pollFirst();
        }
    }

    public static Deque<String> getCombatLog() {
        return combatLog;
    }

    public static void clearLog() {
        if (combatLog != null) {
            combatLog.clear();
        }
    }

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

    public static void setPlayers(List<Player> playerList){
        for(Player player1 : playerList){
            players.add(player1);
        }
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
        arena.strategy.TurnOrderStrategy strategy = new arena.strategy.SpeedBasedTurnOrder();
        turnOrder = strategy.buildTurnOrder(players, enemies.get(0)); 
    }

    public static boolean getUpdateStatus(){
        return updateStatus;
    }

    public static Player getPlayer(){
        return players.get(0);
    }

    public static int getCurrentRound(){
        return currentRound;
    }

    public static Combatant getCurrentAttacker(){
        return turnOrder.get(0);
    }

    public static int getEnemyCount(List<List<Enemy>> enemies){
        if (enemies == null || enemies.isEmpty()) {
            return 0;
        }
        return enemies.get(0).size();
    }

    public static List<Enemy> getCurrentWave(){
        if (enemies == null || enemies.isEmpty()) {
            return new ArrayList<>();
        }
        return enemies.get(0);
    }

    public static List<Combatant> getTurnOrder(){
        return turnOrder;
    }

    public static int checkEndCondition(){  //if enemies are dead, returns 1
        if (enemies == null || enemies.isEmpty() || (enemies.size() == 1 && enemies.get(0).isEmpty())) {
            return 1;                       //if no condition fulfilled, returns 0, game continues
        }
        else{
            for(Player player1 : players ){
                if(!player1.isAlive()){
                    return 2;                   //if players are dead, returns 2, game ends
                }
            }
            return 0;                
        }
    }
    
    public static boolean advanceWaveIfCleared() {
        if (enemies != null && !enemies.isEmpty() && enemies.get(0).isEmpty()) {
            enemies.remove(0); // Pop empty wave
            if (!enemies.isEmpty() && !enemies.get(0).isEmpty()) {
                addLog("A new wave of enemies appears!");
                newTurnOrder();
                return true;
            }
        }
        return false;
    }

}
