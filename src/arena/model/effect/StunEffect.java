package arena.model.effect;

import arena.model.combatant.Combatant;

public class StunEffect extends StatusEffect {
    public static final int DEFAULT_DURATION = 2;

    public StunEffect() {
        this(DEFAULT_DURATION);
    }

    public StunEffect(int turns) {
        super("Stun", turns);
    }

    @Override
    public boolean onTurnStart(Combatant target) {
        if (remainingTurns > 0) {
            remainingTurns--;
            return true;
        }
        return false;
    }
}
