package arena.engine.qte;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;

import arena.ui.screen.TimingBarQteView;

public class CustomModeQtePolicy implements QtePolicy {
    private final Screen screen;
    private final MultiWindowTextGUI gui;

    public CustomModeQtePolicy(Screen screen, MultiWindowTextGUI gui) {
        this.screen = screen;
        this.gui = gui;
    }

    @Override
    public QteResult resolveAttackQte() {
        return TimingBarQteView.runAttack(screen, gui);
    }
}
