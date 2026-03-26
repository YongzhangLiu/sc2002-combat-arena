package arena.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class SpriteCatalog {
    private static final Path ROOT = Paths.get("assets", "sprites");

    private SpriteCatalog() {
    }

    public static AsciiSprite loadBest(String category, String name, int maxWidth, int maxHeight) throws IOException {
        AsciiSprite normal = load(category, name, "normal");
        if (normal.fits(maxWidth, maxHeight)) {
            return normal;
        }

        AsciiSprite compact = load(category, name, "compact");
        if (compact.fits(maxWidth, maxHeight)) {
            return compact;
        }

        return compact;
    }

    public static AsciiSprite load(String category, String name, String variant) throws IOException {
        Path filePath = ROOT.resolve(category).resolve(name + "_" + variant + ".txt");
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        return new AsciiSprite(lines);
    }
}
