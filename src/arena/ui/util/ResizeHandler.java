package arena.ui.util;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import java.util.function.Consumer;

public final class ResizeHandler {
    private TerminalResizeListener listener;
    private final Screen screen;
    private final TextGUI gui;

    private ResizeHandler(Screen screen, TextGUI gui) {
        this.screen = screen;
        this.gui = gui;
    }

    public static ResizeHandler attach(Screen screen, TextGUI gui, Consumer<TerminalSize> onResize) {
        ResizeHandler handler = new ResizeHandler(screen, gui);
        handler.setup(onResize);
        return handler;
    }

    private void setup(Consumer<TerminalSize> onResize) {
        if (!(screen instanceof TerminalScreen terminalScreen)) {
            return;
        }
        Terminal terminal = terminalScreen.getTerminal();
        if (terminal == null) {
            return;
        }
        listener = new TerminalResizeListener() {
            @Override
            public void onResized(Terminal terminal, TerminalSize newSize) {
                if (gui != null) {
                    gui.getGUIThread().invokeLater(() -> onResize.accept(newSize));
                } else {
                    onResize.accept(newSize);
                }
            }
        };
        terminal.addResizeListener(listener);
    }

    public void detach() {
        if (listener == null || !(screen instanceof TerminalScreen terminalScreen)) {
            return;
        }
        Terminal terminal = terminalScreen.getTerminal();
        if (terminal != null) {
            terminal.removeResizeListener(listener);
        }
        listener = null;
    }
}
