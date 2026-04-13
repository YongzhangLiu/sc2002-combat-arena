package arena.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arena.model.combatant.Enemy;
import arena.model.combatant.Goblin;
import arena.model.combatant.Player;
import arena.model.combatant.Warrior;
import arena.model.combatant.Wizard;
import arena.model.combatant.Wolf;
import arena.model.item.Potion;
import arena.model.item.PowerStone;
import arena.model.item.SmokeBomb;

public class GameInit{
    private List<Player> player1 = new ArrayList<Player>();
    private List<List<Enemy>> enemies;

    public Player initPlayers(int choice, String name){        //choice -> 1:warrior, 2:Wizard
        
        if (choice == 1){
            player1.add(new Warrior(name));
        }else{
            player1.add(new Wizard(name));
        }
        return player1.get(player1.size()-1);
    }

    public void chooseItems(Player player1, int choice){        //choice -> 1:Potion,2:PowerStone,3:SmokeBomb
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

    public List<List<Enemy>> initEnemies(int difficulty){ //receive difficulty(1-3) from input
        List<Enemy> firstEnemies = new ArrayList<>();
        List<Enemy> backup = new ArrayList<>();
        enemies = new ArrayList<>(Arrays.asList(firstEnemies, backup));
        
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

    public void startGame(){
        GameState.resetState();
        GameState.setPlayers(player1);
        GameState.setEnemies(enemies);
        GameState.setCurrentRound(1);
        GameState.newTurnOrder();
    }

}
