package arena.model.item;

import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Player;
import arena.model.effect.SmokeBombEffect;

public class SmokeBomb extends Item {
    public SmokeBomb() {
        super("Smoke Bomb");
        setDescription("Grants 100% evasion for 2 turns.");
    }

    @Override
    public void use(Player user, List<Combatant> enemies, Combatant target) {
        user.addStatusEffect(new SmokeBombEffect());
    }
}
