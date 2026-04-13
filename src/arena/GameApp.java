package arena;

import arena.ui.GameStartRequest;
import arena.ui.StartMenu;
import arena.ui.screen.ArenaBattleScreen;
import arena.ui.model.ArenaViewState;
import arena.engine.GameInit;
import arena.engine.GameState;
import arena.engine.BattleEngine;
import arena.engine.mode.GameMode;
import arena.engine.mode.GameModeFactory;
import arena.engine.qte.CustomModeQtePolicy;
import arena.engine.qte.NoOpQtePolicy;
import arena.engine.qte.QtePolicy;
import arena.engine.qte.QteResult;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import arena.ui.util.ScreenUtil;
import arena.model.combatant.Combatant;
import java.io.IOException;
import java.util.List;

public class GameApp {
    private Screen screen;
    private MultiWindowTextGUI gui;
    private arena.ui.GameSetup activeSetup;
    private arena.ui.GameSetup pendingReplaySetup;
    private GameMode activeMode;
    private QtePolicy qtePolicy = new NoOpQtePolicy();
    private boolean sessionFullScreen = true;
    private boolean sessionAsciiMode = false;

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
            StartMenu.launch(screen, gui, true, false, this::startGameSession);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeTerminal();
        }
    }
    
    private void startGameSession(GameStartRequest request) {
        arena.ui.GameSetup setup = request.setup();
        sessionFullScreen = request.fullScreen();
        sessionAsciiMode = request.asciiMode();
        activeSetup = cloneSetup(setup);
        pendingReplaySetup = null;
        activeMode = GameModeFactory.fromSetup(setup);
        qtePolicy = activeMode.usesQte()
            ? new CustomModeQtePolicy(screen, gui)
            : new NoOpQtePolicy();
        GameInit init = new arena.engine.GameInit();
        
        // 1. Init Player Class
        int classChoice = "Wizard".equalsIgnoreCase(setup.playerClass) ? 2 : 1;
        arena.model.combatant.Player player = init.initPlayers(classChoice, "Player");
        
        // 2. Add up to two selected items (None is allowed).
        applySelectedItem(init, player, setup.itemSlot1);
        applySelectedItem(init, player, setup.itemSlot2);
        
        // 3. Init Enemies by selected mode
        activeMode.initializeEnemies(init, setup);
        
        // 4. Start Game State
        init.startGame();
        
        // 5. Connect UI
        BattleEngine engine = new BattleEngine();

        ArenaBattleScreen battleScreen = new ArenaBattleScreen();
        battleScreen.initialize(screen, gui, sessionFullScreen, sessionAsciiMode);
        
        // 6. Set Callbacks for Turn Resolution
        battleScreen.setCallbacks(new ArenaBattleScreen.ActionCallbacks() {
            @Override
            public void onBasicAttack(int targetIndex) {
                QteResult qte = qtePolicy.resolveAttackQte();
                int state = engine.executePlayerTurn(1, targetIndex, null, qte.multiplier());
                handlePlayerTurn(state, battleScreen, engine);
            }
            
            @Override
            public void onDefend() {
                int state = engine.executePlayerTurn(2, 0, null);
                handlePlayerTurn(state, battleScreen, engine);
            }
            
            @Override
            public void onUseItem(int itemIndex, int targetIndex) {
                arena.model.item.Item item = null;
                if (GameState.getPlayer() != null && itemIndex >= 0 && itemIndex < GameState.getPlayer().getInventory().size()) {
                    item = GameState.getPlayer().getInventory().get(itemIndex);
                }
                int state = engine.executePlayerTurn(3, targetIndex, item);
                handlePlayerTurn(state, battleScreen, engine);
            }
            
            @Override
            public void onSpecialSkill(int targetIndex) {
                int state = engine.executePlayerTurn(4, targetIndex, null);
                handlePlayerTurn(state, battleScreen, engine);
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

        if (pendingReplaySetup != null) {
            arena.ui.GameSetup replaySetup = pendingReplaySetup;
            pendingReplaySetup = null;
            startGameSession(new GameStartRequest(replaySetup, sessionFullScreen, sessionAsciiMode));
        }
    }
    
    private void handlePlayerTurn(int state, ArenaBattleScreen battleScreen, BattleEngine engine) {
        java.util.List<arena.engine.BattleEngine.TurnFrame> frames = engine.drainTurnFrames();
        for (arena.engine.BattleEngine.TurnFrame frame : frames) {
            if (frame.getMessage() != null && !frame.getMessage().isBlank()) {
                GameState.addLog(frame.getMessage());
            }
            Integer playerDamage = frame.getPlayerDamage();
            java.util.List<Integer> enemyDamages = frame.getEnemyDamages();
            Integer snapshotPlayerHp = frame.getSnapshotPlayerHp();
            java.util.Map<arena.model.combatant.Combatant, Integer> snapshotEnemyHps = frame.getSnapshotEnemyHps();

            int playerOffset = (playerDamage != null && playerDamage > 0) ? -1 : 0;
            int[] enemyOffsets = buildEnemyDamageOffsets(enemyDamages);
            battleScreen.setDamageOffsets(playerOffset, enemyOffsets);
            battleScreen.render(arena.ui.model.ArenaViewStateMapper.fromGameState(false, false, frame.getMessage(), playerDamage, enemyDamages, snapshotPlayerHp, snapshotEnemyHps));
            refreshScreen();
            try { Thread.sleep(230); } catch (InterruptedException e) {}
            battleScreen.setDamageOffsets(0, null);
            battleScreen.render(arena.ui.model.ArenaViewStateMapper.fromGameState(false, false, frame.getMessage(), playerDamage, enemyDamages, snapshotPlayerHp, snapshotEnemyHps));
            refreshScreen();
            try { Thread.sleep(620); } catch (InterruptedException e) {}
        }

        boolean isGameOver = checkEndCondition(state, true);
        if (isGameOver) {
            battleScreen.close(); // Close battle screen, return control and then show endgame screen
        } else {
            // Update battle UI with new states
            battleScreen.render(arena.ui.model.ArenaViewStateMapper.fromGameState(false, false, "Round resolved."));
        }
    }
    
    private void refreshScreen() {
        try {
            if (gui != null && screen != null) {
                gui.updateScreen();
                screen.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] buildEnemyDamageOffsets(List<Integer> enemyDamages) {
        List<Combatant> wave = GameState.getCurrentWave();
        int n = wave != null ? wave.size() : 0;
        int[] offsets = new int[n];
        if (enemyDamages == null || n == 0) {
            return offsets;
        }
        for (int i = 0; i < n && i < enemyDamages.size(); i++) {
            Integer d = enemyDamages.get(i);
            offsets[i] = (d != null && d > 0) ? 1 : 0;
        }
        return offsets;
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
            
            arena.ui.screen.EndgameScreen.show(screen, gui, sessionFullScreen, sessionAsciiMode, victory, hp, totalRounds, enemiesRemaining, lastLog, new arena.ui.screen.EndgameScreen.EndgameCallbacks() {
                @Override
                public void onReplaySameSettings() {
                    GameState.clearLog();
                    pendingReplaySetup = cloneSetup(activeSetup);
                }

                @Override
                public void onStartNewGame() {
                    GameState.clearLog(); // clear the log specifically on endgame
                    pendingReplaySetup = null;
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

    private void applySelectedItem(GameInit init, arena.model.combatant.Player player, String itemName) {
        if (itemName == null || "None".equalsIgnoreCase(itemName)) {
            return;
        }

        int itemChoice = 1;
        if ("Power Stone".equalsIgnoreCase(itemName)) {
            itemChoice = 2;
        } else if ("Smoke Bomb".equalsIgnoreCase(itemName)) {
            itemChoice = 3;
        }
        init.chooseItems(player, itemChoice);
    }

    private arena.ui.GameSetup cloneSetup(arena.ui.GameSetup source) {
        arena.ui.GameSetup clone = new arena.ui.GameSetup(
            source != null ? source.playerClass : "Warrior",
            source != null ? source.difficulty : "Easy"
        );
        if (source != null) {
            clone.itemSlot1 = source.itemSlot1;
            clone.itemSlot2 = source.itemSlot2;
            clone.customOpponentType = source.customOpponentType;
            clone.customQteEnabled = source.customQteEnabled;
        }
        return clone;
    }
    
    public static void main(String[] args) {
        try {
            new GameApp().run();
        } catch (IOException e) {
            System.err.println("Failed to start GameApp: " + e.getMessage());
        }
    }
}
