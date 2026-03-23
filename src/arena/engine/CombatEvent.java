package arena.engine;

/**
 * A single combat event (e.g., "Goblin attacks Warrior for 25 damage").
 * Immutable, posted to event log after each action.
 */
public class CombatEvent {
    private final String actor;
    private final String action;
    private final String description;

    public CombatEvent(String actor, String action, String description) {
        this.actor = actor;
        this.action = action;
        this.description = description;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", actor, action, description);
    }
}
