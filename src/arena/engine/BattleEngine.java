package arena.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arena.engine.controller.AiTurnController;
import arena.engine.controller.AiTurnResult;
import arena.engine.controller.RandomAiTurnController;
import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class BattleEngine{
    public static class TurnFrame {
        private final String message;
        private final Integer playerDamage;
        private final List<Integer> enemyDamages;
        private final int snapshotPlayerHp;
        private final int snapshotPlayerMaxHp;
        private final Map<Combatant, Integer> snapshotEnemyHps;

        public TurnFrame(String message, Integer playerDamage, List<Integer> enemyDamages,
                         int snapshotPlayerHp, int snapshotPlayerMaxHp,
                         Map<Combatant, Integer> snapshotEnemyHps) {
            this.message = message;
            this.playerDamage = playerDamage;
            this.enemyDamages = enemyDamages == null ? null : new ArrayList<>(enemyDamages);
            this.snapshotPlayerHp = snapshotPlayerHp;
            this.snapshotPlayerMaxHp = snapshotPlayerMaxHp;
            this.snapshotEnemyHps = snapshotEnemyHps == null ? null : new HashMap<>(snapshotEnemyHps);
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

        public int getSnapshotPlayerHp() {
            return snapshotPlayerHp;
        }

        public int getSnapshotPlayerMaxHp() {
            return snapshotPlayerMaxHp;
        }

        public Map<Combatant, Integer> getSnapshotEnemyHps() {
            return snapshotEnemyHps;
        }
    }

    PlayerAction playerAction = new PlayerAction();
    private Player player;
    private int lastPlayerDamage = 0;
    private List<Integer> lastEnemyDamages = new ArrayList<>();
    private final List<TurnFrame> pendingTurnFrames = new ArrayList<>();
    private final AiTurnController aiTurnController = new RandomAiTurnController();

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

    private TurnFrame createTurnFrame(String message, Integer playerDamage, List<Integer> enemyDamages) {
        int pHp = player != null ? player.getHp() : 0;
        int pMaxHp = player != null ? player.getMaxHp() : 0;
        Map<Combatant, Integer> enemyHps = new HashMap<>();
        List<Combatant> wave = GameState.getCurrentWave();
        if (wave != null) {
            for (Combatant e : wave) {
                enemyHps.put(e, e.getHp());
            }
        }
        return new TurnFrame(message, playerDamage, enemyDamages, pHp, pMaxHp, enemyHps);
    }

    public void endRound(){
        int currentRound = GameState.getCurrentRound();
        GameState.setCurrentRound(currentRound+1);
        
        player = GameState.getPlayer();
        if (player != null) {
            player.decrementCooldownAfterTurn();
        }
        for (Combatant combatant : GameState.getCurrentWave()) {
            if (combatant instanceof Player cpuPlayer) {
                cpuPlayer.decrementCooldownAfterTurn();
            }
        }
        
        GameState.newTurnOrder(); // Ensure turn order is rebuilt for the next round
    }

    /**
     * Resolves one full round after the player chooses an action.
     * All combatants act according to current speed-based turn order.
     */
    public int executePlayerTurn(int playerChoice, int target, Item item){
        return executePlayerTurn(playerChoice, target, item, 1.0);
    }

    public int executePlayerTurn(int playerChoice, int target, Item item, double attackMultiplier){
        player = GameState.getPlayer();
        List<Combatant> currentWave = GameState.getCurrentWave();
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
                if (actingPlayer == player) {
                    if (playerActionConsumed) {
                        continue;
                    }
                    if (actingPlayer.beginTurn()) {
                        executeChosenPlayerAction(playerChoice, target, item, currentWave, actingPlayer, attackMultiplier);
                        String playerActionMessage = switch (playerChoice) {
                            case 1 -> actingPlayer.getName() + " attacks.";
                            case 2 -> actingPlayer.getName() + " defends.";
                            case 3 -> actingPlayer.getName() + " uses an item.";
                            case 4 -> actingPlayer.getName() + " casts " + (actingPlayer instanceof arena.model.combatant.Warrior ? "Shield Bash." : "Arcane Blast.");
                            default -> actingPlayer.getName() + " acts.";
                        };
                        pendingTurnFrames.add(createTurnFrame(playerActionMessage, null, lastEnemyDamages));
                    }
                    playerActionConsumed = true;
                } else {
                    if (actingPlayer.beginTurn() && player.isAlive()) {
                        executeAiPlayerAction(actingPlayer, player);
                    }
                }
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
                    String actionMessage = enemy.getName() + " attacks.";
                    Integer frameDamage = damage > 0 ? damage : null;
                    lastPlayerDamage = frameDamage != null ? frameDamage : 0;
                    pendingTurnFrames.add(createTurnFrame(actionMessage, frameDamage, null));
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

    private void executeChosenPlayerAction(int playerChoice, int target, Item item, List<Combatant> currentWave, Player actingPlayer, double attackMultiplier) {
        if (currentWave == null || currentWave.isEmpty()) {
            return;
        }

        int targetIndex = Math.max(0, Math.min(target, currentWave.size() - 1));
        Combatant targetEnemy = currentWave.get(targetIndex);

        List<Integer> hpBefore = new ArrayList<>();
        for (Combatant e : currentWave) {
            hpBefore.add(e.getHp());
        }

        switch (playerChoice) {
            case 1: // basic attack
                applyScaledAttack(actingPlayer, targetEnemy, attackMultiplier);
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

    private void applyScaledAttack(Player attacker, Combatant target, double multiplier) {
        int baseDamage = Math.max(0, attacker.getAttack() - target.getDefense());
        int scaledDamage = (int) Math.round(baseDamage * multiplier);
        target.takeDamage(scaledDamage);
    }

    private void executeAiPlayerAction(Player aiPlayer, Player humanTarget) {
        AiTurnResult result = aiTurnController.resolveTurn(aiPlayer, humanTarget);
        Integer damage = result.getDamageToPlayer();
        pendingTurnFrames.add(createTurnFrame(result.getMessage(), damage, null));
        lastPlayerDamage = damage != null ? damage : 0;
    }

    public void sweepDeadEnemies() {
        List<Combatant> currentWave = GameState.getCurrentWave();
        if (currentWave != null) {
            boolean removed = false;
            for (int i = currentWave.size() - 1; i >= 0; i--) {
                Combatant e = currentWave.get(i);
                if (!e.isAlive()) {
                    pendingTurnFrames.add(createTurnFrame(e.getName() + " was defeated!", null, null));
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
