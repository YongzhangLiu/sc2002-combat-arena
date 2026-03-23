package arena.model.effect;

import arena.model.combatant.Combatant;
import arena.model.combatant.Enemy;

public class SmokeBombEffect extends StatusEffect {
    public SmokeBombEffect() {
        this(2);
    }

    public SmokeBombEffect(int turns) {
        super("Smoke Bomb Invulnerability", turns);
    }

    @Override
    public boolean onTurnStart(Combatant target) {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
        return false;
    }

    @Override
    public int modifyIncomingDamage(int incomingDamage, Combatant attacker, Combatant target) {
        if (attacker instanceof Enemy) {
            return 0;
        }
        return incomingDamage;
    }
}
