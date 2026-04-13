package arena.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import arena.engine.GameState;
import arena.model.combatant.Combatant;
import arena.model.combatant.Player;
import arena.model.item.Item;

public class ArenaViewStateMapper {

    public static ArenaViewState fromGameState(boolean victory, boolean defeat, String message) {
        return fromGameState(victory, defeat, message, null, null, null, null);
    }

    public static ArenaViewState fromGameState(boolean victory, boolean defeat, String message, Integer floatingDamage) {
        return fromGameState(victory, defeat, message, floatingDamage, null, null, null);
    }

    public static ArenaViewState fromGameState(boolean victory, boolean defeat, String message, Integer floatingDamage, List<Integer> enemyDamages) {
        return fromGameState(victory, defeat, message, floatingDamage, enemyDamages, null, null);
    }

    public static ArenaViewState fromGameState(boolean victory, boolean defeat, String message,
                                               Integer floatingDamage, List<Integer> enemyDamages,
                                               Integer overridePlayerHp, Map<Combatant, Integer> overrideEnemyHps) {
        Player player = GameState.getPlayer();
        List<Combatant> wave = GameState.getCurrentWave();

        // Build Player State
        PlayerViewState playerState = null;
        if (player != null) {
            int displayHp = overridePlayerHp != null ? overridePlayerHp : player.getHp();
            boolean hasItems = player.getInventory() != null && !player.getInventory().isEmpty();
            playerState = new PlayerViewState(
                player.getName(),
                player.getDescription(),
                player.getClass().getSimpleName(),
                displayHp,
                player.getMaxHp(),
                player.getAttack(),
                player.getDefense(),
                player.getSpeed(),
                player.getClass().getSimpleName().toLowerCase(),
                player.getSpecialSkillCooldown(),
                player.canUseSpecialSkill(),
                hasItems,
                Collections.emptyList(),
                floatingDamage
            );
        }

        // Build Enemy States
        List<EnemyViewState> enemyStates = new ArrayList<>();
        if (wave != null) {
            for (int i = 0; i < wave.size(); i++) {
                Combatant e = wave.get(i);
                int displayHp = (overrideEnemyHps != null && overrideEnemyHps.containsKey(e))
                    ? overrideEnemyHps.get(e) : e.getHp();
                Integer edmg = null;
                if (enemyDamages != null && i < enemyDamages.size()) {
                    edmg = enemyDamages.get(i);
                    if (edmg != null && edmg <= 0) edmg = null;
                }
                enemyStates.add(new EnemyViewState(
                    i,
                    e.getName(),
                    e.getDescription(),
                    e.getClass().getSimpleName(),
                    displayHp,
                    e.getMaxHp(),
                    e.getAttack(),
                    e.getDefense(),
                    e.getSpeed(),
                    e.getClass().getSimpleName().toLowerCase(),
                    e.isAlive(),
                    Collections.emptyList(),
                    edmg
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

        String specialSkillDescription = "";
        if (player != null) {
            if ("Warrior".equals(player.getClass().getSimpleName())) {
                specialSkillDescription = "Shield Bash: Deals moderate damage and stunlocks the target.";
            } else if ("Wizard".equals(player.getClass().getSimpleName())) {
                specialSkillDescription = "Arcane Blast: Deals damage to all enemies and strengthens your spells.";
            }
        }

        // Item Options
        List<String> availableItems = new ArrayList<>();
        List<String> availableItemDescriptions = new ArrayList<>();
        if (player != null && player.getInventory() != null) {
            for (Item item : player.getInventory()) {
                availableItems.add(item.getName());
                String desc = item.getDescription();
                availableItemDescriptions.add(desc != null ? desc : "");
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
            availableItemDescriptions,
            specialSkillDescription,
            combatLog,
            message,
            victory,
            defeat
        );
    }
}
