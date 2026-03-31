package arena;

import arena.ui.StartMenu;
import arena.ui.screen.ArenaBattleScreen;
import arena.ui.model.ArenaViewState;
import arena.engine.GameInit;
import arena.engine.GameState;
import arena.engine.BattleEngine;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import arena.ui.util.ScreenUtil;
import java.io.IOException;

public class GameApp {
    private Screen screen;
    private MultiWindowTextGUI gui;
    
    public GameApp() throws IOException {
        initializeTerminal();
    }
    
    private void initializeTerminal() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        screen = terminalFactory.createScreen();
        screen.startScreen();
        ScreenUtil.setMouseReporting(screen, true);
        
        gui = new MultiWindowTextGUI(
            screen,
            new DefaultWindowManager(),
            new EmptySpace(TextColor.ANSI.DEFAULT)
        );
        StartMenu.applyUniformButtonTheme(gui); // Needs public access if not already
    }
    
    public void run() {
        try {
            // 1. Launch Start Menu
            StartMenu.launch(screen, gui, false, false, setup -> {
                // TODO: 2. Initialize Engine and Start UI loop
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeTerminal();
        }
    }
    
    private void closeTerminal() {
        try {
            if (screen != null) {
                ScreenUtil.setMouseReporting(screen, false);
                screen.stopScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            new GameApp().run();
        } catch (IOException e) {
            System.err.println("Failed to start GameApp: " + e.getMessage());
        }
    }
}
