package arena.model.item;

import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Player;

public class Potion extends Item {
    private static final int HEAL_AMOUNT = 100;

    public Potion() {
        super("Potion");
        setDescription("Restores 100 HP.");
    }

    @Override
    public void use(Player user, List<Combatant> enemies, Combatant target) {
        user.heal(HEAL_AMOUNT);
    }
}
