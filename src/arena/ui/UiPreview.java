package arena.ui;

import arena.ui.screen.ArenaBattleScreen;
import arena.ui.screen.ArenaPreviewStateFactory;
import arena.ui.screen.EndgameScreen;

import java.io.IOException;

/**
 * Isolated preview application for running entirely within the UI-only maven profile.
 * Scaffolds the start menu and injects mock responses to preview the battle screens.
 */
public final class UiPreview {
    private UiPreview() {
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting UI Preview Mode...");
        
        // Uncomment the screen you want to test
        previewStartMenu();
        // previewEndgameScreen();
    }

    private static void previewStartMenu() throws IOException {
        System.out.println("Previewing Start Menu...");
        // Boot start menu and intercept the callback
        StartMenu.launch(false, false, setup -> {
            System.out.println("Setup Completed. Previewing Battle Screen...");
            
            try {
                ArenaBattleScreen arenaScreen = new ArenaBattleScreen();
                // Initialize default terminal settings for the preview combat screen
                com.googlecode.lanterna.screen.Screen screen = new com.googlecode.lanterna.terminal.DefaultTerminalFactory().createScreen();
                screen.startScreen();
                arena.ui.util.ScreenUtil.setMouseReporting(screen, true);
                
                com.googlecode.lanterna.gui2.MultiWindowTextGUI gui = new com.googlecode.lanterna.gui2.MultiWindowTextGUI(
                    screen, 
                    new com.googlecode.lanterna.gui2.DefaultWindowManager(), 
                    new com.googlecode.lanterna.gui2.EmptySpace(com.googlecode.lanterna.TextColor.ANSI.DEFAULT)
                );
                
                arenaScreen.initialize(screen, gui, false, false);
                
                // Mount mock data
                arenaScreen.showAndWait(ArenaPreviewStateFactory.fromSetup(setup));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void previewEndgameScreen() {
        try {
            System.out.println("Previewing Endgame Screen...");
            com.googlecode.lanterna.screen.Screen screen = new com.googlecode.lanterna.terminal.DefaultTerminalFactory().createScreen();
            screen.startScreen();
            arena.ui.util.ScreenUtil.setMouseReporting(screen, true);
            
            com.googlecode.lanterna.gui2.MultiWindowTextGUI gui = new com.googlecode.lanterna.gui2.MultiWindowTextGUI(
                screen, 
                new com.googlecode.lanterna.gui2.DefaultWindowManager(), 
                new com.googlecode.lanterna.gui2.EmptySpace(com.googlecode.lanterna.TextColor.ANSI.DEFAULT)
            );

            EndgameScreen.show(
                screen,
                gui,
                false, // not fullscreen for preview
                false, // test asciiMode false
                false, // test defeat
                0,     // playerHp
                2,    // totalRounds
                3,     // enemiesRemaining
                "You were killed by Goblin!", // lastLogEntry
                new EndgameScreen.EndgameCallbacks() {
                    @Override
                    public void onBackToMenu() {
                        System.out.println("User clicked Back to Menu");
                        try { screen.stopScreen(); } catch (Exception ignored) {}
                    }

                    @Override
                    public void onQuit() {
                        System.out.println("User clicked Quit");
                        try { screen.stopScreen(); } catch (Exception ignored) {}
                        System.exit(0);
                    }
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
