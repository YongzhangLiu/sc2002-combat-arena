package arena.engine.mode;

import arena.engine.GameInit;
import arena.ui.GameSetup;

public interface GameMode {
    String id();
    void initializeEnemies(GameInit init, GameSetup setup);
    boolean usesQte();
}
