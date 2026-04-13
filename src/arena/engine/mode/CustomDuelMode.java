package arena.engine.mode;

import arena.engine.GameInit;
import arena.ui.GameSetup;

public class CustomDuelMode implements GameMode {
    private final boolean qteEnabled;

    public CustomDuelMode(boolean qteEnabled) {
        this.qteEnabled = qteEnabled;
    }

    @Override
    public String id() {
        return "custom_duel";
    }

    @Override
    public void initializeEnemies(GameInit init, GameSetup setup) {
        String type = setup != null && setup.customOpponentType != null
            ? setup.customOpponentType
            : "Random";
        init.initCustomEnemies(type);
    }

    @Override
    public boolean usesQte() {
        return qteEnabled;
    }
}
