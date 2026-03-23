package arena.engine;

/**
 * Public contract for UI to communicate with the engine.
 * Engine owns all domain logic; UI is read-only.
 *
 * Usage:
 *  1. UI calls getCurrentState() to render
 *  2. Player inputs an action → UI creates PlayerCommand
 *  3. UI calls executePlayerAction(command)
 *  4. Engine validates, applies rules, logs events
 *  5. Engine returns new BattleViewState
 *  6. UI re-renders from new state
 */
public interface BattleEngineFacade {

    /**
     * Get current battle state snapshot for rendering.
     * Read-only; UI must never mutate.
     */
    BattleViewState getCurrentState();

    /**
     * Execute a player action command.
     * Engine validates, processes, and returns updated state.
     */
    BattleViewState executePlayerAction(PlayerCommand command);

    /**
     * Process all enemy turns for the current round.
     * Engine auto-applies AI and returns updated state.
     */
    BattleViewState processEnemyTurns();

    /**
     * Start a new battle session.
     */
    void initializeBattle(String playerClass, int difficulty);

    /**
     * Check if the game has ended (win/loss condition).
     */
    boolean isGameOver();
}
