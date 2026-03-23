package arena.engine;

import arena.model.action.Action;
import arena.model.item.Item;

/**
 * Represents a command from the UI to the engine.
 * Immutable data structure for safe communication.
 */
public class PlayerCommand {
    private final Action action;
    private final Object target; // Combatant or Item, depending on action

    public PlayerCommand(Action action, Object target) {
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
