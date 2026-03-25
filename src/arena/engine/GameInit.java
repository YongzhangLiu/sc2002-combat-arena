package arena.engine;

import java.util.ArrayList;
import java.util.Arrays;

import arena.model.combatant.Enemy;
import arena.model.combatant.Goblin;
import arena.model.combatant.Player;
import arena.model.combatant.Warrior;
import arena.model.combatant.Wizard;
import arena.model.combatant.Wolf;
import arena.model.item.Potion;
import arena.model.item.PowerStone;
import arena.model.item.SmokeBomb;

public class GameInit {
    private ArrayList<Enemy> firstEnemies,backup;
    private Player player1;
    ;

    public Player initPlayers(int choice, String name){        //choice -> 1:warrior, 2:Wizard
        if (choice == 1){
            player1 = new Warrior(name);
        }else{
            player1 = new Wizard(name);
        }
        return player1;
    }

    public void chooseItems(int choice){        //choice -> 1:Potion,2:PowerStone,3:SmokeBomb
        switch (choice) {
            case 1:
                player1.addItem(new Potion());
                break;
            case 2:
                player1.addItem(new PowerStone());
                break;
            case 3:
                player1.addItem(new SmokeBomb());
                break;
        
            default:
                break;
        }
    }

    public ArrayList<ArrayList<Enemy>> initEnemies(int difficulty){ //receive difficulty(1-3) from input
        ArrayList<ArrayList<Enemy>> enemies = new ArrayList<>(Arrays.asList(firstEnemies, backup));

        switch (difficulty) {
            case 1:     //easy;
                for (int i = 0; i < 3; i++){
                    firstEnemies.add(new Goblin("Goblin " + Integer.toString(i+1)));
                }
                return enemies;

            case 2:     //medium
                firstEnemies.add(new Goblin("Goblin 1"));
                firstEnemies.add(new Wolf("Wolf 1"));
                for (int i = 0; i < 2; i++){
                    backup.add(new Wolf("Wolf " + Integer.toString(i+1)));
                }
                return enemies;

            case 3:     //hard
                for (int i = 0; i < 2; i++){
                    firstEnemies.add(new Goblin("Goblin " + Integer.toString(i+1)));
                }
                backup.add(new Goblin("Goblin 3"));
                for (int x = 1; x < 3; x++){
                    backup.add(new Wolf("Wolf " + Integer.toString(x)));
                }
                return enemies;
        
            default:
                return enemies;
        }
    }
}
