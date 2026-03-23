package arena.model.action;

import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.effect.DefendBuff;

public class Defend extends Action {
    public Defend() {
        super("Defend");
    }

    @Override
    public void execute(Combatant actor, Combatant target, List<? extends Combatant> opponents) {
        if (actor == null || !actor.isAlive()) {
            return;
        }
        actor.addStatusEffect(new DefendBuff());
    }
}
