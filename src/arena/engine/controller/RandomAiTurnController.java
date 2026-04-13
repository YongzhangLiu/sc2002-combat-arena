package arena.engine.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import arena.engine.PlayerAction;
import arena.model.combatant.Player;
import arena.model.item.Item;
import arena.model.item.PowerStone;

public class RandomAiTurnController implements AiTurnController {
    private final PlayerAction playerAction;
    private final Random random;

    public RandomAiTurnController() {
        this.playerAction = new PlayerAction();
        this.random = new Random();
    }

    @Override
    public AiTurnResult resolveTurn(Player aiPlayer, Player humanTarget) {
        List<String> availableActions = new ArrayList<>();
        availableActions.add("attack");
        availableActions.add("defend");
        if (!aiPlayer.getInventory().isEmpty()) {
            availableActions.add("item");
        }
        if (aiPlayer.canUseSpecialSkill()) {
            availableActions.add("special");
        }

        String picked = availableActions.get(random.nextInt(availableActions.size()));
        Integer damage = null;
        switch (picked) {
            case "attack" -> {
                int hpBefore = humanTarget.getHp();
                playerAction.bAttack(aiPlayer, humanTarget);
                damage = hpBefore - humanTarget.getHp();
                return new AiTurnResult(aiPlayer.getName() + " used BasicAttack.", damage > 0 ? damage : null);
            }
            case "defend" -> {
                playerAction.defend(aiPlayer);
                return new AiTurnResult(aiPlayer.getName() + " used Defend.", null);
            }
            case "item" -> {
                Item chosen = aiPlayer.getInventory().isEmpty() ? null : aiPlayer.getInventory().get(random.nextInt(aiPlayer.getInventory().size()));
                int hpBefore = humanTarget.getHp();
                playerAction.consumeItem(aiPlayer, humanTarget, List.of(humanTarget), chosen);
                if (chosen instanceof PowerStone) {
                    damage = hpBefore - humanTarget.getHp();
                }
                String itemName = chosen != null ? chosen.getName() : "Item";
                return new AiTurnResult(aiPlayer.getName() + " used " + itemName + ".", damage != null && damage > 0 ? damage : null);
            }
            case "special" -> {
                int hpBefore = humanTarget.getHp();
                playerAction.specialSkill(aiPlayer, humanTarget, List.of(humanTarget));
                damage = hpBefore - humanTarget.getHp();
                String skillName = aiPlayer instanceof arena.model.combatant.Warrior ? "Shield Bash" : "Arcane Blast";
                return new AiTurnResult(aiPlayer.getName() + " used " + skillName + ".", damage > 0 ? damage : null);
            }
            default -> {
                return new AiTurnResult(aiPlayer.getName() + " hesitated.", null);
            }
        }
    }
}
