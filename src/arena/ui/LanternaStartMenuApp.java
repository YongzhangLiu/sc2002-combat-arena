package arena.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class LanternaStartMenuApp {

    public static void main(String[] args) throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory()
            .setInitialTerminalSize(new TerminalSize(40, 12));

        Screen screen = terminalFactory.createScreen();
        screen.startScreen();

        try {
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
            BasicWindow window = new BasicWindow();

            Panel panel = new Panel();
            panel.setLayoutManager(new GridLayout(1));

            panel.addComponent(new Button("New Game", () -> {
            }));
            panel.addComponent(new Button("View Controls", () -> {
            }));
            panel.addComponent(new Button("Options", () -> {
            }));
            panel.addComponent(new Button("Exit", window::close));

            window.setComponent(panel);
            window.setHints(java.util.Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.CENTERED));

            gui.addWindowAndWait(window);
        } finally {
            screen.stopScreen();
        }
    }
}
