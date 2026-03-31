package arena.ui.util;

import arena.model.combatant.Combatant;

/**
 * Temporary mapper utility to extract strings from Combatant.
 * To be replaced by proper Controller mappings in the future.
 */
public final class ViewMapperUtil {
    
    private ViewMapperUtil() {
    }

    public static String combatantStatBlock(Combatant combatant) {
        return "HP: " + combatant.getMaxHp() + "\n"
            + "ATK: " + combatant.getAttack() + "\n"
            + "DEF: " + combatant.getBaseDefense() + "\n"
            + "SPD: " + combatant.getSpeed();
    }
}
