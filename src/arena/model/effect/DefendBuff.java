package arena.model.effect;

import arena.model.combatant.Combatant;

public class DefendBuff extends StatusEffect {
    private final int defenseBoost;

    public DefendBuff() {
        this(2, 10);
    }

    public DefendBuff(int turns, int defenseBoost) {
        super("Defend", turns);
        this.defenseBoost = defenseBoost;
    }

    @Override
    public boolean onTurnStart(Combatant target) {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
        return false;
    }

    @Override
    public int modifyDefense(int baseDefense) {
        return baseDefense + defenseBoost;
    }
}
