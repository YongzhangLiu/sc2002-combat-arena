package arena.engine;


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
    private final List<String> availableActions; // ["BasicAttack", "Defend", "UseItem", "SpecialSkill"]
    private final EventLog eventLog;
    private final GamePhase phase; // SELECTION, IN_BATTLE, VICTORY, DEFEAT

    public BattleViewState(int currentRound, CombatantView playerView,
                           List<CombatantView> enemyViews, boolean isPlayerTurn,
                           List<String> availableActions, EventLog eventLog,
                           GamePhase phase) {
        this.currentRound = currentRound;
        this.playerView = playerView;
        this.enemyViews = enemyViews;
        this.isPlayerTurn = isPlayerTurn;
        this.availableActions = availableActions;
        this.eventLog = eventLog;
        this.phase = phase;
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

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public EventLog getEventLog() {
        return eventLog;
    }

    public GamePhase getPhase() {
        return phase;
    }
}
