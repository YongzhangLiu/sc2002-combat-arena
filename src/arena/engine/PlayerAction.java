package arena.engine;

import java.util.ArrayList;
import java.util.List;

import arena.model.action.Action;
import arena.model.action.BasicAttack;
import arena.model.action.Defend;
import arena.model.action.UseItem;
import arena.model.action.UseSpecialSkill;
import arena.model.combatant.Enemy;
import arena.model.combatant.Goblin;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class PlayerAction {
    private List<Enemy> tempList = new ArrayList<>();
    private Enemy tempEnemy = new Goblin(null);    

    public void bAttack(Player player1, Enemy target){
        Action bAttack = new BasicAttack();
        bAttack.execute(player1, target, tempList);
    }
    public void defend(Player player1){
        Action defence = new Defend();
        defence.execute(player1, tempEnemy, tempList);
    }

    public void specialSkill(Player player1, Enemy target, List<Enemy> enemies){
        Action specialSkill = new UseSpecialSkill();
        specialSkill.execute(player1, target, enemies);
    }

    public void consumeItem(Player player1, Enemy target, List<Enemy> enemies, Item item){
        Action consumeItem = new UseItem(item);
        consumeItem.execute(player1, target, enemies);
    }
}
