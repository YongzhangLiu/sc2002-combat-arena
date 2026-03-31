package arena.ui;

import arena.ui.screen.ArenaBattleScreen;
import arena.ui.screen.ArenaPreviewStateFactory;

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
        
        // Boot start menu and intercept the callback
        StartMenu.launch(false, false, setup -> {
            System.out.println("Setup Completed. Previewing Battle Screen...");
            
            try {
                ArenaBattleScreen arenaScreen = new ArenaBattleScreen();
                // Initialize default terminal settings for the preview combat screen
                com.googlecode.lanterna.screen.Screen screen = new com.googlecode.lanterna.terminal.DefaultTerminalFactory().createScreen();
                screen.startScreen();
                
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
}
