package arena.engine;

import arena.model.action.Action;

import java.util.Collections;
import java.util.List;

/**
 * Represents a command from the UI to the engine.
 * Immutable data structure for safe communication.
 */
public class PlayerCommand {
    private final Action action;
    private final Object target; // Combatant or Item, depending on action
    private final String selectedPlayerClass;
    private final String selectedDifficulty;
    private final List<String> selectedItems;

    public PlayerCommand(Action action, Object target) {

        this(action, target, null, null, Collections.emptyList());
    }

    public PlayerCommand(Action action, Object target,
                         String selectedPlayerClass,
                         String selectedDifficulty,
                         List<String> selectedItems) {
        this.action = action;
        this.target = target;
        this.selectedPlayerClass = selectedPlayerClass;
        this.selectedDifficulty = selectedDifficulty;
        this.selectedItems = selectedItems == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(selectedItems);
    }

    public static PlayerCommand forInitialization(String selectedPlayerClass,
                                                  String selectedDifficulty,
                                                  List<String> selectedItems) {
        return new PlayerCommand(null, null, selectedPlayerClass, selectedDifficulty, selectedItems);
    }

    public static PlayerCommand forBattleAction(Action action, Object target) {
        return new PlayerCommand(action, target);
    }

    public Action getAction() {
        return action;
    }

    public Object getTarget() {
        return target;
    }

    public String getSelectedPlayerClass() {
        return selectedPlayerClass;
    }

    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public List<String> getSelectedItems() {
        return selectedItems;
    }
}
