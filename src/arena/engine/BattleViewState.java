package arena.engine;


import java.util.Collections;
import java.util.List;

/**
 * Snapshot of the current battle state for UI rendering.
 * Engine returns this after each action; UI renders from it.
 * Immutable: UI never mutates.
 */
public class BattleViewState {
    private final int currentRound;
    private final CombatantView playerView;
    private final List<CombatantView> enemyViews;
    private final boolean isPlayerTurn;
    private final String currentActorName;
    private final List<String> turnOrder;
    private final List<String> availableActions; // ["BasicAttack", "Defend", "UseItem", "SpecialSkill"]
    private final List<String> availableTargets;
    private final List<String> selectedItems;
    private final String selectedDifficulty;
    private final int enemiesRemaining;
    private final int roundsSurvived;
    private final boolean replayAvailable;// SELECTION, IN_BATTLE, VICTORY, DEFEAT

    public BattleViewState(int currentRound, CombatantView playerView,
                           List<CombatantView> enemyViews, boolean isPlayerTurn,
                           List<String> availableActions) {
        this(currentRound, playerView, enemyViews, isPlayerTurn, null,
            Collections.emptyList(), availableActions, Collections.emptyList(),
            Collections.emptyList(), null, enemyViews == null ? 0 : (int) enemyViews.stream().filter(CombatantView::isAlive).count(),
            currentRound, false);
    }

    public BattleViewState(int currentRound, CombatantView playerView,
                           List<CombatantView> enemyViews, boolean isPlayerTurn,
                           String currentActorName, List<String> turnOrder,
                           List<String> availableActions, List<String> availableTargets,
                           List<String> selectedItems, String selectedDifficulty,
                           int enemiesRemaining, int roundsSurvived,
                           boolean replayAvailable) {
        this.currentRound = currentRound;
        this.playerView = playerView;
        this.enemyViews = enemyViews;
        this.isPlayerTurn = isPlayerTurn;
        this.currentActorName = currentActorName;
        this.turnOrder = turnOrder;
        this.availableActions = availableActions;
        this.availableTargets = availableTargets;
        this.selectedItems = selectedItems;
        this.selectedDifficulty = selectedDifficulty;
        this.enemiesRemaining = enemiesRemaining;
        this.roundsSurvived = roundsSurvived;
        this.replayAvailable = replayAvailable;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public CombatantView getPlayerView() {
        return playerView;
    }

    public List<CombatantView> getEnemyViews() {
        return enemyViews;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public String getCurrentActorName() {
        return currentActorName;
    }

    public List<String> getTurnOrder() {
        return turnOrder;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public List<String> getAvailableTargets() {
        return availableTargets;
    }

    public List<String> getSelectedItems() {
        return selectedItems;
    }

    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public int getEnemiesRemaining() {
        return enemiesRemaining;
    }

    public int getRoundsSurvived() {
        return roundsSurvived;
    }

    public boolean isReplayAvailable() {
        return replayAvailable;
    }

}
