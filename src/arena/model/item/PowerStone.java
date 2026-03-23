package arena.model.item;

import java.util.List;

import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public class PowerStone extends Item {
    public PowerStone() {
        super("Power Stone");
    }

    @Override
    public void use(Player user, List<Enemy> enemies, Enemy target) {
        user.triggerSpecialSkillWithoutCooldown(enemies, target);
    }
}
