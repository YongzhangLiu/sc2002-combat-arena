package arena.ui.screen;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.terminal.Terminal;

import arena.engine.qte.QteResult;

/**
 * Attack QTE: reads {@link Terminal} input so keys are not swallowed by the GUI after button clicks.
 */
public final class TimingBarQteView {
    private static final int TOTAL_STEPS = 18;
    private static final long STEP_MS = 12L;
    private static final int PERFECT_TAIL_STEPS = 5;

    private TimingBarQteView() {}

    public static QteResult runAttack(Screen screen, MultiWindowTextGUI gui) {
        if (screen == null || gui == null) {
            return QteResult.NORMAL;
        }
        if (!(screen instanceof TerminalScreen terminalScreen)) {
            return QteResult.NORMAL;
        }

        Terminal terminal = terminalScreen.getTerminal();
        drainInput(terminal);

        BasicWindow window = new BasicWindow();
        window.setHints(java.util.Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS, Window.Hint.NO_POST_RENDERING));
        window.setFixedSize(new TerminalSize(46, 7));
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label("QTE: Attack"));
        panel.addComponent(new Label("Press Z near the end of the bar for double damage"));
        panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        Label barLabel = new Label(barText(0, TOTAL_STEPS));
        panel.addComponent(barLabel);
        window.setComponent(panel);
        gui.addWindow(window);

        int perfectStart = TOTAL_STEPS - PERFECT_TAIL_STEPS + 1;
        if (perfectStart < 0) {
            perfectStart = 0;
        }

        boolean pressed = false;
        int pressStep = -1;

        for (int step = 0; step <= TOTAL_STEPS; step++) {
            barLabel.setText(barText(step, TOTAL_STEPS));

            try {
                gui.updateScreen();
                screen.refresh();
            } catch (IOException ignored) { }

            KeyStroke keyStroke = pollFromTerminal(terminal);
            while (keyStroke != null) {
                if (!pressed && isZ(keyStroke)) {
                    pressed = true;
                    pressStep = step;
                }
                keyStroke = pollFromTerminal(terminal);
            }

            try {
                Thread.sleep(STEP_MS);
            } catch (InterruptedException ignored) { }
        }

        window.close();

        if (!pressed) {
            return QteResult.NORMAL;
        }
        if (pressStep >= perfectStart) {
            return QteResult.PERFECT;
        }
        return QteResult.EARLY;
    }

    private static String barText(int filled, int totalSteps) {
        String filledPart = "#".repeat(filled);
        String emptyPart = " ".repeat(Math.max(0, totalSteps - filled));
        return "[" + filledPart + emptyPart + "]";
    }

    private static void drainInput(Terminal terminal) {
        if (terminal == null) {
            return;
        }
        KeyStroke k;
        while ((k = pollFromTerminal(terminal)) != null) {
            // discard queued strokes (e.g. from the action button)
        }
    }

    private static KeyStroke pollFromTerminal(Terminal terminal) {
        if (terminal == null) {
            return null;
        }
        try {
            return terminal.pollInput();
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isZ(KeyStroke keyStroke) {
        if (keyStroke == null) {
            return false;
        }
        if (keyStroke.getKeyType() == KeyType.Character) {
            Character c = keyStroke.getCharacter();
            return c != null && (c == 'z' || c == 'Z');
        }
        return false;
    }
}
