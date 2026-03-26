package arena.ui;

import java.io.IOException;
import java.util.function.Consumer;

public final class LanternaUiEntryPoint {
    private LanternaUiEntryPoint() {
    }

    public static void runStartMenu() throws IOException {
        LanternaStartMenuApp.launch(false, false, null);
    }

    public static void runStartMenu(boolean fullScreen, boolean asciiMode) throws IOException {
        LanternaStartMenuApp.launch(fullScreen, asciiMode, null);
    }

    public static void runStartMenu(boolean fullScreen, boolean asciiMode, Consumer<GameSetup> onSetupReady) throws IOException {
        LanternaStartMenuApp.launch(fullScreen, asciiMode, onSetupReady);
    }
}
