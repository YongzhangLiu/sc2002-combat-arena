package arena.model.action;

import java.util.List;

import arena.model.combatant.Combatant;

public abstract class Action {
    private final String name;

    protected Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void execute(Combatant actor, Combatant target, List<? extends Combatant> opponents);
}
