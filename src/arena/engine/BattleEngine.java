package arena.engine;

import java.util.ArrayList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class BattleEngine{
    public static class TurnFrame {
        private final String message;
        private final Integer playerDamage;
        private final List<Integer> enemyDamages;

        public TurnFrame(String message, Integer playerDamage, List<Integer> enemyDamages) {
            this.message = message;
            this.playerDamage = playerDamage;
            this.enemyDamages = enemyDamages == null ? null : new ArrayList<>(enemyDamages);
        }

        public String getMessage() {
            return message;
        }

        public Integer getPlayerDamage() {
            return playerDamage;
        }

        public List<Integer> getEnemyDamages() {
            return enemyDamages;
        }
    }

    PlayerAction playerAction = new PlayerAction();
    private Player player;
    private int lastPlayerDamage = 0;
    private List<Integer> lastEnemyDamages = new ArrayList<>();
    private final List<TurnFrame> pendingTurnFrames = new ArrayList<>();

    public int getLastPlayerDamage() {
        return lastPlayerDamage;
    }

    public List<Integer> getLastEnemyDamages() {
        return lastEnemyDamages;
    }

    public List<TurnFrame> drainTurnFrames() {
        List<TurnFrame> frames = new ArrayList<>(pendingTurnFrames);
        pendingTurnFrames.clear();
        return frames;
    }

    public void endRound(){
        int currentRound = GameState.getCurrentRound();
        GameState.setCurrentRound(currentRound+1);
        
        player = GameState.getPlayer();
        if (player != null) {
            player.decrementCooldownAfterTurn();
        }
        
        GameState.newTurnOrder(); // Ensure turn order is rebuilt for the next round
    }

    /**
     * Resolves one full round after the player chooses an action.
     * All combatants act according to current speed-based turn order.
     */
    public int executePlayerTurn(int playerChoice, int target, Item item){
        player = GameState.getPlayer();
        List<Enemy> currentWave = GameState.getCurrentWave();
        if (player == null || currentWave == null) {
            return GameState.checkEndCondition();
        }

        lastPlayerDamage = 0;
        lastEnemyDamages.clear();
        pendingTurnFrames.clear();

        boolean playerActionConsumed = false;
        List<Combatant> turnOrderSnapshot = new ArrayList<>(GameState.getTurnOrder());

        for (Combatant combatant : turnOrderSnapshot) {
            if (GameState.checkEndCondition() != 0) {
                break;
            }
            if (combatant == null || !combatant.isAlive()) {
                continue;
            }

            if (combatant instanceof Player actingPlayer) {
                if (playerActionConsumed || actingPlayer != player) {
                    continue;
                }
                if (actingPlayer.beginTurn()) {
                    executeChosenPlayerAction(playerChoice, target, item, currentWave, actingPlayer);
                    String playerActionMessage = switch (playerChoice) {
                        case 1 -> actingPlayer.getName() + " attacks.";
                        case 2 -> actingPlayer.getName() + " defends.";
                        case 3 -> actingPlayer.getName() + " uses an item.";
                        case 4 -> actingPlayer.getName() + " casts " + (actingPlayer instanceof arena.model.combatant.Warrior ? "Shield Bash." : "Arcane Blast.");
                        default -> actingPlayer.getName() + " acts.";
                    };
                    pendingTurnFrames.add(new TurnFrame(playerActionMessage, null, lastEnemyDamages));
                }
                playerActionConsumed = true;
                sweepDeadEnemies();
                continue;
            }

            if (combatant instanceof Enemy enemy) {
                if (enemy.beginTurn() && enemy.getStrategy() != null && player.isAlive()) {
                    List<Player> targetList = new ArrayList<>();
                    targetList.add(player);
                    int hpBefore = player.getHp();
                    enemy.getStrategy().execute(enemy, targetList);
                    int hpAfter = player.getHp();
                    int damage = hpBefore - hpAfter;
                    lastPlayerDamage = damage;
                    pendingTurnFrames.add(new TurnFrame(enemy.getName() + " attacks.", damage > 0 ? damage : null, null));
                }
                sweepDeadEnemies();
            }
        }

        int endCondition = GameState.checkEndCondition();
        if (endCondition == 0) {
            endRound();
            endCondition = GameState.checkEndCondition();
        }
        return endCondition;
    }

    /**
     * Fast-forwards the engine through enemy turns until it's the Player's turn again, 
     * or until the wave/game ends.
     * Use this strictly for letting the engine process automatically without UI input.
     * Returns 0 for continue, 1 for win, 2 for loss.
     */
    public int advanceTurnQueue() {
        return GameState.checkEndCondition();
    }

    private void executeChosenPlayerAction(int playerChoice, int target, Item item, List<Enemy> currentWave, Player actingPlayer) {
        if (currentWave == null || currentWave.isEmpty()) {
            return;
        }

        int targetIndex = Math.max(0, Math.min(target, currentWave.size() - 1));
        Enemy targetEnemy = currentWave.get(targetIndex);

        List<Integer> hpBefore = new ArrayList<>();
        for (Enemy e : currentWave) {
            hpBefore.add(e.getHp());
        }

        switch (playerChoice) {
            case 1: // basic attack
                playerAction.bAttack(actingPlayer, targetEnemy);
                break;
            case 2: // defend
                playerAction.defend(actingPlayer);
                break;
            case 3: // item
                playerAction.consumeItem(actingPlayer, targetEnemy, currentWave, item);
                break;
            case 4: // special
                playerAction.specialSkill(actingPlayer, targetEnemy, currentWave);
                break;
            default:
                break;
        }

        lastEnemyDamages.clear();
        for (int i = 0; i < currentWave.size(); i++) {
            int damage = hpBefore.get(i) - currentWave.get(i).getHp();
            lastEnemyDamages.add(damage);
        }
    }

    public void sweepDeadEnemies() {
        List<Enemy> currentWave = GameState.getCurrentWave();
        if (currentWave != null) {
            boolean removed = false;
            for (int i = currentWave.size() - 1; i >= 0; i--) {
                Enemy e = currentWave.get(i);
                if (!e.isAlive()) {
                    pendingTurnFrames.add(new TurnFrame(e.getName() + " was defeated!", null, null));
                    currentWave.remove(i);
                    GameState.getTurnOrder().remove(e);
                    removed = true;
                }
            }
            if (removed) {
                GameState.setCurrentWave(currentWave);
            }
        }
        GameState.advanceWaveIfCleared();
    }
}
