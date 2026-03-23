package arena.model.item;

import java.util.List;

import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public class Potion extends Item {
    private static final int HEAL_AMOUNT = 100;

    public Potion() {
        super("Potion");
    }

    @Override
    public void use(Player user, List<Enemy> enemies, Enemy target) {
        user.heal(HEAL_AMOUNT);
    }
}
