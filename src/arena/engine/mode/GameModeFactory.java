package arena.engine.mode;

import arena.ui.GameSetup;

public final class GameModeFactory {
    private GameModeFactory() {}

    public static GameMode fromSetup(GameSetup setup) {
        if (setup != null && "Custom".equalsIgnoreCase(setup.difficulty)) {
            return new CustomDuelMode(setup.customQteEnabled);
        }
        int diffChoice = 1;
        if (setup != null) {
            if ("Medium".equalsIgnoreCase(setup.difficulty)) {
                diffChoice = 2;
            } else if ("Hard".equalsIgnoreCase(setup.difficulty)) {
                diffChoice = 3;
            }
        }
        return new StandardMode(diffChoice);
    }
}
