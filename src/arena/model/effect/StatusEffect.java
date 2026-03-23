package arena.model.effect;

import arena.model.combatant.Combatant;

public abstract class StatusEffect {
    private final String name;
    protected int remainingTurns;

    protected StatusEffect(String name, int turns) {
        this.name = name;
        this.remainingTurns = Math.max(0, turns);
    }

    public String getName() {
        return name;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public boolean isExpired() {
        return remainingTurns <= 0;
    }

    public void onApply(Combatant target) {
        // Default no-op.
    }

    public void onExpire(Combatant target) {
        // Default no-op.
    }

    // Returns true when the target should skip this turn.
    public boolean onTurnStart(Combatant target) {
        return false;
    }

    public int modifyDefense(int baseDefense) {
        return baseDefense;
    }

    public int modifyIncomingDamage(int incomingDamage, Combatant attacker, Combatant target) {
        return incomingDamage;
    }
}
