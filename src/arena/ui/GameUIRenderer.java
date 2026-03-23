package arena.ui;

import arena.engine.BattleViewState;

/**
 * Contract for rendering the current battle state.
 * UI implementations render from BattleViewState snapshots.
 * Never mutates domain objects.
 */
public interface GameUIRenderer {

    /**
     * Render the main menu screen.
     */
    void renderMainMenu();

    /**
     * Render character selection screen.
     */
    void renderCharacterSelection();

    /**
     * Render difficulty selection screen.
     */
    void renderDifficultySelection();

    /**
     * Render the battle arena using the current state snapshot.
     * Called after each action to refresh the display.
     */
    void renderBattleState(BattleViewState state);

    /**
     * Render victory screen with final statistics.
     */
    void renderVictory(BattleViewState finalState);

    /**
     * Render defeat screen with remaining enemy count and rounds survived.
     */
    void renderDefeat(BattleViewState finalState);

    /**
     * Wait for and return the player's action choice.
     * Called during player turn; UI collects input and returns command.
     */
    PlayerActionInput waitForPlayerAction(BattleViewState currentState);

    /**
     * Display a brief message (e.g., action result, status change).
     */
    void displayMessage(String message);

    /**
     * Clear the screen (if terminal supports it).
     */
    void clearScreen();
}
