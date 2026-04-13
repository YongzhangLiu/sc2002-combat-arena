package arena.model.item;

import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Player;

public abstract class Item {
    private final String name;
    private String description = "";

    protected Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract void use(Player user, List<Combatant> enemies, Combatant target);
}
