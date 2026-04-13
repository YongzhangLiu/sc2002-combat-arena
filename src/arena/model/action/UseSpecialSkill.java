package arena.model.action;

import java.util.ArrayList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Player;

public class UseSpecialSkill extends Action {
    public UseSpecialSkill() {
        super("UseSpecialSkill");
    }

    @Override
    public void execute(Combatant actor, Combatant target, List<? extends Combatant> opponents) {
        if (!(actor instanceof Player player)) {
            throw new IllegalArgumentException("UseSpecialSkill can only be executed by a player.");
        }

        List<Combatant> enemyList = new ArrayList<>();
        if (opponents != null) {
            for (Combatant combatant : opponents) {
                enemyList.add(combatant);
            }
        }

        player.useSpecialSkill(enemyList, target);
    }
}
