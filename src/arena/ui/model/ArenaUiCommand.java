package arena.ui.model;

/**
 * Sealed command interface for all UI -> engine interactions.
 * 
 * UI never mutates engine state directly; all changes go through commands.
 * Engine receives command, validates, applies rules, returns updated ArenaViewState.
 */
public sealed interface ArenaUiCommand permits
    ArenaUiCommand.SelectAction,
    ArenaUiCommand.SelectItem,
    ArenaUiCommand.SelectTarget,
    ArenaUiCommand.ConfirmAction,
    ArenaUiCommand.Cancel {

    /**
     * Player selected an action (e.g., "BasicAttack", "Defend").
     */
    record SelectAction(String actionName) implements ArenaUiCommand {}

    /**
     * Player selected an item to use.
     */
    record SelectItem(String itemName) implements ArenaUiCommand {}

    /**
     * Player selected a target enemy by index.
     */
    record SelectTarget(int targetIndex) implements ArenaUiCommand {}

    /**
     * Player confirmed the current action/item/target selection.
     * Engine validates and executes.
     */
    record ConfirmAction() implements ArenaUiCommand {}

    /**
     * Player cancelled current selection (return to prior state).
     */
    record Cancel() implements ArenaUiCommand {}
}
