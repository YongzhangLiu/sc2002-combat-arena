package arena.ui;

import arena.model.action.Action;

/**
 * Encapsulates a player's action choice from the UI.
 * Minimal data structure for safe communication.
 */
public class PlayerActionInput {
    private final Action action;
    private final Object target; // Target combatant or item

    public PlayerActionInput(Action action, Object target) {
        this.action = action;
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public Object getTarget() {
        return target;
    }
}
