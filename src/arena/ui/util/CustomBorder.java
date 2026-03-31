package arena.ui.util;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractBorder;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public class CustomBorder extends AbstractBorder {
    private final boolean asciiMode;

    public CustomBorder(boolean asciiMode) {
        super();
        this.asciiMode = asciiMode;
    }

    @Override
    protected BorderRenderer createDefaultRenderer() {
        return new BorderRenderer() {
            @Override
            public TerminalPosition getWrappedComponentTopLeftOffset() {
                return new TerminalPosition(1, 1);
            }

            @Override
            public TerminalSize getWrappedComponentSize(TerminalSize boundingBox) {
                return new TerminalSize(
                        Math.max(0, boundingBox.getColumns() - 2),
                        Math.max(0, boundingBox.getRows() - 2)
                );
            }

            @Override
            public TerminalSize getPreferredSize(Border component) {
                Component wrapped = component.getComponent();
                if (wrapped == null) {
                    return new TerminalSize(2, 2);
                }
                TerminalSize preferredSize = wrapped.getPreferredSize();
                return new TerminalSize(preferredSize.getColumns() + 2, preferredSize.getRows() + 2);
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, Border component) {
                TerminalSize size = graphics.getSize();
                if (size.getColumns() < 2 || size.getRows() < 2) {
                    return; // Too small to draw a border
                }
                
                Component wrapped = component.getComponent();
                if (wrapped != null) {
                    TextGUIGraphics innerGraphics = graphics.newTextGraphics(
                            getWrappedComponentTopLeftOffset(),
                            getWrappedComponentSize(size));
                    wrapped.draw(innerGraphics);
                }

                graphics.applyThemeStyle(component.getThemeDefinition().getNormal());
                
                char horizontal = asciiMode ? '-' : '─';
                char vertical = asciiMode ? '|' : '│';
                char topLeft = asciiMode ? '+' : '╭';
                char topRight = asciiMode ? '+' : '╮';
                char bottomLeft = asciiMode ? '+' : '╰';
                char bottomRight = asciiMode ? '+' : '╯';
                
                // Draw corners
                graphics.setCharacter(0, 0, topLeft);
                graphics.setCharacter(size.getColumns() - 1, 0, topRight);
                graphics.setCharacter(0, size.getRows() - 1, bottomLeft);
                graphics.setCharacter(size.getColumns() - 1, size.getRows() - 1, bottomRight);
                
                // Draw horizontal lines
                for (int x = 1; x < size.getColumns() - 1; x++) {
                    graphics.setCharacter(x, 0, horizontal);
                    graphics.setCharacter(x, size.getRows() - 1, horizontal);
                }
                
                // Draw vertical lines
                for (int y = 1; y < size.getRows() - 1; y++) {
                    graphics.setCharacter(0, y, vertical);
                    graphics.setCharacter(size.getColumns() - 1, y, vertical);
                }
            }
        };
    }
}
