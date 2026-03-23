package arena.model.action;

import java.util.List;

import arena.model.combatant.Combatant;

public class BasicAttack extends Action {
    public BasicAttack() {
        super("BasicAttack");
    }

    @Override
    public void execute(Combatant actor, Combatant target, List<? extends Combatant> opponents) {
        if (actor == null || target == null) {
            throw new IllegalArgumentException("Actor and target are required for BasicAttack.");
        }
        if (!actor.isAlive() || !target.isAlive()) {
            return;
        }
        int damage = Math.max(0, actor.getAttack() - target.getDefense());
        target.takeDamage(damage, actor);
    }
}
