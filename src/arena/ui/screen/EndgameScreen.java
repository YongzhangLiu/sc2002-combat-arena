package arena.ui.screen;

import arena.ui.util.DialogComposer;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static arena.ui.util.ScreenUtil.dialogSizeForScreen;

/**
 * Full-screen visual replacement for game-over sequences.
 */
public class EndgameScreen {

    public interface EndgameCallbacks {
        void onBackToMenu();
        void onQuit();
    }

    public static void show(Screen screen, MultiWindowTextGUI gui, boolean fullScreen, boolean asciiMode, boolean isVictory, String reasonMessage, EndgameCallbacks callbacks) {
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        
        TerminalSize dialogSize = dialogSizeForScreen(screen, fullScreen);
        int rows = dialogSize.getRows();
        int cols = dialogSize.getColumns();
        
        if (fullScreen) {
            window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING, Window.Hint.FULL_SCREEN, Window.Hint.EXPANDED));
        } else {
            window.setFixedSize(dialogSize);
        }

        Panel mainPanel = new Panel(new LinearLayout());

        // Attempt to load the ascii art for win/loss
        List<String> bannerLines = loadEndgameBanner(isVictory);
        int bannerHeight = bannerLines.size();
        
        // Calculate vertical spacing mathematically
        int paddingRows = Math.max(1, (rows - bannerHeight - 12) / 2);
        
        DialogComposer.addVerticalPaddingTop(mainPanel, rows, paddingRows);

        // Sub-panel for the ascii text, centered
        for (String line : bannerLines) {
            mainPanel.addComponent(DialogComposer.centered(new Label(line)));
        }

        mainPanel.addComponent(new EmptySpace(new TerminalSize(1, 2)));

        // Create actual bordered inner-box for details
        int contentWidth = Math.max(40, cols / 2);
        
        Panel innerContent = new Panel(new LinearLayout());
        
        String title = isVictory ? "YOU WIN!" : "YOU LOSE";
        innerContent.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        innerContent.addComponent(DialogComposer.centered(new Label(title)));
        innerContent.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        
        // Reason formatting ("You were slain by goblin")
        String message = reasonMessage != null ? reasonMessage : "";
        innerContent.addComponent(DialogComposer.centered(new Label(message)));
        innerContent.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        // Buttons fit inside the dialog box
        innerContent.addComponent(DialogComposer.centered(new Button("Back to Menu", () -> {
            window.close();
            if (callbacks != null) callbacks.onBackToMenu();
        })));
        
        innerContent.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        innerContent.addComponent(DialogComposer.centered(new Button("Quit", () -> {
            window.close();
            if (callbacks != null) callbacks.onQuit();
        })));
        
        innerContent.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        
        // Ensure it maintains width
        innerContent.setPreferredSize(new TerminalSize(contentWidth, 10));

        // Use our new CustomBorder to get rounded corners (or ASCII mode fallback)
        com.googlecode.lanterna.gui2.Border borderBox = new arena.ui.util.CustomBorder(asciiMode);
        borderBox.setComponent(innerContent);
        
        // 1. Center the dialog box itself horizontally
        mainPanel.addComponent(DialogComposer.centered(borderBox));

        DialogComposer.addVerticalPaddingBottom(mainPanel, rows, paddingRows);

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
    }

    private static List<String> loadEndgameBanner(boolean isVictory) {
        String filename = isVictory ? "win.txt" : "loss.txt";
        Path bannerPath = Paths.get("assets", "sprites", "end", filename);
        try {
            if (Files.exists(bannerPath)) {
                return Files.readAllLines(bannerPath, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            // Suppress fallback error in UI loop
        }
        
        // Fallback if file isn't found
        return isVictory 
            ? List.of("=== VICTORY ===") 
            : List.of("=== DEFEAT ===");
    }
}
