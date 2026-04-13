package arena.engine.controller;

import arena.model.combatant.Player;

public interface AiTurnController {
    AiTurnResult resolveTurn(Player aiPlayer, Player humanTarget);
}
