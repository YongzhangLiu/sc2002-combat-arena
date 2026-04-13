package arena.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import arena.model.combatant.Combatant;
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
    private List<List<Combatant>> enemies;
    private final Random random = new Random();

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

    public List<List<Combatant>> initEnemies(int difficulty){ //receive difficulty(1-3) from input
        List<Combatant> firstEnemies = new ArrayList<>();
        List<Combatant> backup = new ArrayList<>();
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

    /**
     * Custom duel: one opponent. Types: Random, Warrior, Wizard, Goblin, Wolf.
     * CPU Warriors/Wizards get two random items (duplicates allowed). Goblins/Wolves do not use items.
     */
    public List<List<Combatant>> initCustomEnemies(String opponentType) {
        List<Combatant> firstEnemies = new ArrayList<>();
        List<Combatant> backup = new ArrayList<>();
        enemies = new ArrayList<>(Arrays.asList(firstEnemies, backup));

        String normalized = opponentType == null ? "Random" : opponentType.trim();
        if (normalized.isEmpty()) {
            normalized = "Random";
        }

        if ("Random".equalsIgnoreCase(normalized)) {
            String[] pool = { "Warrior", "Wizard", "Goblin", "Wolf" };
            normalized = pool[random.nextInt(pool.length)];
        }

        Combatant opponent;
        if ("Warrior".equalsIgnoreCase(normalized)) {
            Player cpu = new Warrior("CPU Warrior");
            addRandomItem(cpu);
            addRandomItem(cpu);
            opponent = cpu;
        } else if ("Wizard".equalsIgnoreCase(normalized)) {
            Player cpu = new Wizard("CPU Wizard");
            addRandomItem(cpu);
            addRandomItem(cpu);
            opponent = cpu;
        } else if ("Goblin".equalsIgnoreCase(normalized)) {
            opponent = new Goblin("CPU Goblin");
        } else if ("Wolf".equalsIgnoreCase(normalized)) {
            opponent = new Wolf("CPU Wolf");
        } else {
            Player cpu = new Warrior("CPU Warrior");
            addRandomItem(cpu);
            addRandomItem(cpu);
            opponent = cpu;
        }

        firstEnemies.add(opponent);
        return enemies;
    }

    private void addRandomItem(Player player) {
        int itemChoice = random.nextInt(3) + 1;
        chooseItems(player, itemChoice);
    }

    public void startGame(){
        GameState.resetState();
        GameState.setPlayers(player1);
        GameState.setEnemies(enemies);
        GameState.setCurrentRound(1);
        GameState.newTurnOrder();
    }

}
