package arena.model.action;

import java.util.ArrayList;
import java.util.List;

import arena.model.combatant.Combatant;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class UseItem extends Action {
    private final Item item;

    public UseItem(Item item) {
        super("UseItem");
        this.item = item;
    }

    @Override
    public void execute(Combatant actor, Combatant target, List<? extends Combatant> opponents) {
        if (!(actor instanceof Player player)) {
            throw new IllegalArgumentException("UseItem can only be executed by a player.");
        }
        if (item == null) {
            throw new IllegalStateException("No item selected.");
        }

        List<Combatant> opponentList = new ArrayList<>();
        if (opponents != null) {
            for (Combatant combatant : opponents) {
                opponentList.add(combatant);
            }
        }

        item.use(player, opponentList, target);
        player.removeItem(item);
    }
}
