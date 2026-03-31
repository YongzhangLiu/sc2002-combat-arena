package arena.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arena.engine.GameState;
import arena.model.combatant.Enemy;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class ArenaViewStateMapper {

    public static ArenaViewState fromGameState(boolean victory, boolean defeat, String message) {
        Player player = GameState.getPlayer();
        List<Enemy> wave = GameState.getCurrentWave();

        // Build Player State
        PlayerViewState playerState = null;
        if (player != null) {
            playerState = new PlayerViewState(
                player.getName(),
                player.getClass().getSimpleName(), // e.g., "Warrior" or "Wizard"
                player.getHp(),
                player.getMaxHp(),
                player.getAttack(),
                player.getDefense(),
                player.getSpeed(),
                player.getClass().getSimpleName().toLowerCase(),
                player.getSpecialSkillCooldown(),
                player.canUseSpecialSkill(),
                Collections.emptyList() // Status effects not deeply implemented yet
            );
        }

        // Build Enemy States
        List<EnemyViewState> enemyStates = new ArrayList<>();
        if (wave != null) {
            for (int i = 0; i < wave.size(); i++) {
                Enemy e = wave.get(i);
                enemyStates.add(new EnemyViewState(
                    i,
                    e.getName(),
                    e.getClass().getSimpleName(),
                    e.getHp(),
                    e.getMaxHp(),
                    e.getAttack(),
                    e.getDefense(),
                    e.getSpeed(),
                    e.getClass().getSimpleName().toLowerCase(),
                    e.isAlive(),
                    Collections.emptyList()
                ));
            }
        }

        // Action Options
        List<String> availableActions = new ArrayList<>();
        availableActions.add("BasicAttack");
        availableActions.add("Defend");
        availableActions.add("UseItem");
        if (player != null && player.canUseSpecialSkill()) {
            availableActions.add("SpecialSkill");
        }

        // Item Options
        List<String> availableItems = new ArrayList<>();
        if (player != null && player.getInventory() != null) {
            for (Item item : player.getInventory()) {
                availableItems.add(item.getName());
            }
        }

        // Current combat turn info
        boolean isPlayerTurn = false;
        String turnOwnerName = "";
        if (GameState.getTurnOrder() != null && !GameState.getTurnOrder().isEmpty()) {
            isPlayerTurn = GameState.getTurnOrder().get(0) instanceof Player;
            turnOwnerName = GameState.getTurnOrder().get(0).getName();
        }

        List<String> combatLog = new ArrayList<>(GameState.getCombatLog());

        return new ArenaViewState(
            GameState.getCurrentRound(),
            turnOwnerName,
            isPlayerTurn,
            playerState,
            enemyStates,
            0, // Default target index
            availableActions,
            availableItems,
            combatLog,
            message,
            victory,
            defeat
        );
    }
}
