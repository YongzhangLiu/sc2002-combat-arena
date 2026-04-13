package arena.engine.mode;

import arena.engine.GameInit;
import arena.ui.GameSetup;

public class StandardMode implements GameMode {
    private final int difficulty;

    public StandardMode(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String id() {
        return "standard";
    }

    @Override
    public void initializeEnemies(GameInit init, GameSetup setup) {
        init.initEnemies(difficulty);
    }

    @Override
    public boolean usesQte() {
        return false;
    }
}
