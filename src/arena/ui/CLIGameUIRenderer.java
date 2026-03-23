package arena.ui;

import arena.engine.BattleViewState;

/**
 * Minimal skeletal implementation of GameUIRenderer for testing/structure.
 * Your CLI implementation using Lanterna will replace the render* methods.
 */
public class CLIGameUIRenderer implements GameUIRenderer {

    @Override
    public void renderMainMenu() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void renderCharacterSelection() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void renderDifficultySelection() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void renderBattleState(BattleViewState state) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void renderVictory(BattleViewState finalState) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void renderDefeat(BattleViewState finalState) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public PlayerActionInput waitForPlayerAction(BattleViewState currentState) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void displayMessage(String message) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void clearScreen() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}






















































