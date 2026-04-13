package arena.ui.screen;

// TODO (Phase 3 Phase Out): This class strictly exists so UiPreview can generate
// a mock combat session without engine logic. This class should be deleted
// entirely once the root Controller (GameApp) and Engine are fully mapping real GameStates to ArenaViewStates.

import arena.model.combatant.Combatant;
import arena.model.combatant.Warrior;
import arena.model.combatant.Wizard;
import arena.ui.GameSetup;
import arena.ui.model.ArenaViewState;
import arena.ui.model.EnemyViewState;
import arena.ui.model.PlayerViewState;
import arena.ui.model.StatusIconViewState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds a preview-only ArenaViewState from setup selections.
 *
 * This keeps start-menu integration unblocked while engine battle loop is in progress.
 */
public final class ArenaPreviewStateFactory {
    private static final List<String> ENEMY_TYPES = List.of("goblin", "wolf");

    private ArenaPreviewStateFactory() {
    }

    public static ArenaViewState fromSetup(GameSetup setup) {
        PlayerViewState player = buildPlayer(setup.playerClass);
        List<EnemyViewState> enemies = buildEnemies(enemyCountForDifficulty(setup.difficulty));

        return new ArenaViewState(
            1,
            player.getName(),
            true,
            player,
            enemies,
            enemies.isEmpty() ? 0 : 0,
            List.of("BasicAttack", "Defend", "UseItem", "SpecialSkill"),
            List.of(setup.itemSlot1, setup.itemSlot2),
            List.of("A useful item.", "A useful item."),
            "A powerful special skill.",
            List.of("Battle preview loaded.", "Enemy slots scale with alive enemy count."),
            "Preview mode - input wiring next phase.",
            false,
            false
        );
    }

    private static PlayerViewState buildPlayer(String playerClass) {
        Combatant base = "Wizard".equalsIgnoreCase(playerClass)
            ? new Wizard("Wizard")
            : new Warrior("Warrior");

        return new PlayerViewState(
            base.getName(),
            playerClass,
            base.getMaxHp(),
            base.getMaxHp(),
            base.getAttack(),
            base.getBaseDefense(),
            base.getSpeed(),
            playerClass.toLowerCase(),
            2,
            false,
            true, // hasItems mock
            Collections.<StatusIconViewState>emptyList()
        );
    }

    private static List<EnemyViewState> buildEnemies(int count) {
        List<EnemyViewState> enemies = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String type = ENEMY_TYPES.get(index % ENEMY_TYPES.size());
            int hp = 60 + (index * 15);
            enemies.add(new EnemyViewState(
                index,
                titleCase(type) + " " + (index + 1),
                type,
                hp,
                hp,
                20 + index,
                6 + index,
                8 + index,
                type,
                true,
                Collections.<StatusIconViewState>emptyList()
            ));
        }
        return enemies;
    }

    private static int enemyCountForDifficulty(String difficulty) {
        if (difficulty == null) {
            return 2;
        }
        return switch (difficulty.toLowerCase()) {
            case "hard" -> 4;
            case "medium" -> 3;
            default -> 2;
        };
    }

    private static String titleCase(String value) {
        if (value == null || value.isEmpty()) {
            return "Enemy";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
