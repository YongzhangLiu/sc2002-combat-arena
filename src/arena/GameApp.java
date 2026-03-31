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
                startGameSession(setup);
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeTerminal();
        }
    }
    
    private void startGameSession(arena.ui.GameSetup setup) {
        GameInit init = new arena.engine.GameInit();
        
        // 1. Init Player Class
        int classChoice = "Wizard".equalsIgnoreCase(setup.playerClass) ? 2 : 1;
        arena.model.combatant.Player player = init.initPlayers(classChoice, "Player");
        
        // 2. Add Item
        int itemChoice = 1;
        if ("Power Stone".equalsIgnoreCase(setup.item)) itemChoice = 2;
        else if ("Smoke Bomb".equalsIgnoreCase(setup.item)) itemChoice = 3;
        init.chooseItems(player, itemChoice);
        
        // 3. Init Enemies
        int diffChoice = 1;
        if ("Medium".equalsIgnoreCase(setup.difficulty)) diffChoice = 2;
        else if ("Hard".equalsIgnoreCase(setup.difficulty)) diffChoice = 3;
        init.initEnemies(diffChoice);
        
        // 4. Start Game State
        init.startGame();
        
        // 5. Connect UI
        BattleEngine engine = new BattleEngine();
        
        // Pre-advance turn queue in case an enemy is faster and goes first
        int initialState = engine.advanceTurnQueue();
        if (checkEndCondition(initialState, false)) {
            return;
        }

        ArenaBattleScreen battleScreen = new ArenaBattleScreen();
        battleScreen.initialize(screen, gui, false, false);
        
        // 6. Set Callbacks for Turn Resolution
        battleScreen.setCallbacks(new ArenaBattleScreen.ActionCallbacks() {
            @Override
            public void onBasicAttack(int targetIndex) {
                int state = engine.executePlayerTurn(1, targetIndex, null);
                handleTurnResult(state, battleScreen, engine);
            }
            
            @Override
            public void onDefend() {
                int state = engine.executePlayerTurn(2, 0, null);
                handleTurnResult(state, battleScreen, engine);
            }
            
            @Override
            public void onUseItem(int itemIndex, int targetIndex) {
                arena.model.item.Item item = null;
                if (GameState.getPlayer() != null && itemIndex >= 0 && itemIndex < GameState.getPlayer().getInventory().size()) {
                    item = GameState.getPlayer().getInventory().get(itemIndex);
                }
                int state = engine.executePlayerTurn(3, targetIndex, item);
                handleTurnResult(state, battleScreen, engine);
            }
            
            @Override
            public void onSpecialSkill(int targetIndex) {
                int state = engine.executePlayerTurn(4, targetIndex, null);
                handleTurnResult(state, battleScreen, engine);
            }
            
            @Override
            public void onBackToMenu() {
                GameState.clearLog();
                battleScreen.close();
            }
        });
        
        // 7. Render loops blocking wait call
        ArenaViewState viewState = arena.ui.model.ArenaViewStateMapper.fromGameState(false, false, "Battle Started!");
        battleScreen.showAndWait(viewState);
    }
    
    private void handleTurnResult(int state, ArenaBattleScreen battleScreen, BattleEngine engine) {
        // Continue enemies queue if player ended their turn
        if (state == 0) {
            state = engine.advanceTurnQueue();
        }
        
        boolean isGameOver = checkEndCondition(state, true);
        if (isGameOver) {
            battleScreen.close(); // Close battle screen, return control and then show endgame screen
        } else {
            // Update battle UI with new states
            battleScreen.render(arena.ui.model.ArenaViewStateMapper.fromGameState(false, false, "Turn Advanced."));
        }
    }
    
    private boolean checkEndCondition(int state, boolean triggerEndgameScreen) {
        boolean victory = (state == 1);
        boolean defeat = (state == 2);
        
        if (!victory && !defeat) {
            return false;
        }
        
        if (triggerEndgameScreen) {
            String lastLog = "";
            if (GameState.getCombatLog() != null && !GameState.getCombatLog().isEmpty()) {
                lastLog = GameState.getCombatLog().peekLast();
            }
            int hp = GameState.getPlayer() != null ? GameState.getPlayer().getHp() : 0;
            int totalRounds = GameState.getCurrentRound();
            int enemiesRemaining = GameState.getCurrentWave() != null ? GameState.getCurrentWave().size() : 0;
            
            arena.ui.screen.EndgameScreen.show(screen, gui, false, false, victory, hp, totalRounds, enemiesRemaining, lastLog, new arena.ui.screen.EndgameScreen.EndgameCallbacks() {
                @Override
                public void onBackToMenu() {
                    GameState.clearLog(); // clear the log specifically on endgame
                    // Loop naturally goes back to StartMenu because runSession is in a while loop inside StartMenu
                }

                @Override
                public void onQuit() {
                    System.exit(0);
                }
            });
        }
        
        return true;
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
