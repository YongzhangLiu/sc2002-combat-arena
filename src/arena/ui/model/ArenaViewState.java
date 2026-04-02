package arena.ui.model;

import java.util.Collections;
import java.util.List;

/**
 * Immutable UI-facing snapshot of the current arena battle state.
 * Engine emits this to UI for rendering and interaction.
 *
 * All collections are defensive copies; UI must never mutate.
 */
public class ArenaViewState {
    private final int roundNumber;
    private final String turnOwnerName;
    private final boolean isPlayerTurn;
    private final PlayerViewState playerState;
    private final List<EnemyViewState> aliveEnemies;
    private final int currentTargetIndex; // index into aliveEnemies list
    private final List<String> availableActions; // e.g., ["BasicAttack", "Defend", "UseItem"]
    private final List<String> availableItems;
    private final List<String> availableItemDescriptions;
    private final String specialSkillDescription;
    private final List<String> combatLog; // recent events for display
    private final String feedbackMessage; // current validation/result message
    private final boolean victory;
    private final boolean defeat;

    public ArenaViewState(int roundNumber, String turnOwnerName, boolean isPlayerTurn,
                         PlayerViewState playerState, List<EnemyViewState> aliveEnemies,
                         int currentTargetIndex, List<String> availableActions,
                         List<String> availableItems, List<String> availableItemDescriptions,
                         String specialSkillDescription, List<String> combatLog,
                         String feedbackMessage, boolean victory, boolean defeat) {
        this.roundNumber = roundNumber;
        this.turnOwnerName = turnOwnerName;
        this.isPlayerTurn = isPlayerTurn;
        this.playerState = playerState;
        this.aliveEnemies = aliveEnemies != null
            ? Collections.unmodifiableList(aliveEnemies)
            : Collections.emptyList();
        this.currentTargetIndex = Math.max(0, Math.min(currentTargetIndex, this.aliveEnemies.size() - 1));
        this.availableActions = availableActions != null
            ? Collections.unmodifiableList(availableActions)
            : Collections.emptyList();
        this.availableItems = availableItems != null
            ? Collections.unmodifiableList(availableItems)
            : Collections.emptyList();
        this.availableItemDescriptions = availableItemDescriptions != null
            ? Collections.unmodifiableList(availableItemDescriptions)
            : Collections.emptyList();
        this.specialSkillDescription = specialSkillDescription != null ? specialSkillDescription : "";
        this.combatLog = combatLog != null
            ? Collections.unmodifiableList(combatLog)
            : Collections.emptyList();
        this.feedbackMessage = feedbackMessage != null ? feedbackMessage : "";
        this.victory = victory;
        this.defeat = defeat;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getTurnOwnerName() {
        return turnOwnerName;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public PlayerViewState getPlayerState() {
        return playerState;
    }

    public List<EnemyViewState> getAliveEnemies() {
        return aliveEnemies;
    }

    public int getCurrentTargetIndex() {
        return currentTargetIndex;
    }

    public EnemyViewState getCurrentTarget() {
        if (currentTargetIndex >= 0 && currentTargetIndex < aliveEnemies.size()) {
            return aliveEnemies.get(currentTargetIndex);
        }
        return null;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public List<String> getAvailableItems() {
        return availableItems;
    }

    public List<String> getAvailableItemDescriptions() {
        return availableItemDescriptions;
    }

    public String getSpecialSkillDescription() {
        return specialSkillDescription;
    }

    public List<String> getCombatLog() {
        return combatLog;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public boolean isVictory() {
        return victory;
    }

    public boolean isDefeat() {
        return defeat;
    }

    public boolean isGameOver() {
        return victory || defeat;
    }
}
