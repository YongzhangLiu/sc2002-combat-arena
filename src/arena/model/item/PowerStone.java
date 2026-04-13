package arena.model.item;

import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Player;

public class PowerStone extends Item {
    public PowerStone() {
        super("Power Stone");
        setDescription("Triggers your special skill immediately without using a cooldown.");
    }

    @Override
    public void use(Player user, List<Combatant> enemies, Combatant target) {
        user.triggerSpecialSkillWithoutCooldown(enemies, target);
    }
}
