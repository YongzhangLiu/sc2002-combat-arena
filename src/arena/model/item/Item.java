package arena.model.item;

import java.util.List;

import arena.model.combatant.Enemy;
import arena.model.combatant.Player;

public abstract class Item {
    private final String name;

    protected Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void use(Player user, List<Enemy> enemies, Enemy target);
}
