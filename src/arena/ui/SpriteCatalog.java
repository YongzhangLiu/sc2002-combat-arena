package arena.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public static AsciiSprite loadBestArenaStrip(String name, int maxWidth, int maxHeight) throws IOException {
        AsciiSprite normal = composeTiledArena(name, "normal", maxWidth);
        if (normal != null && normal.fits(maxWidth, maxHeight)) {
            return normal;
        }

        AsciiSprite compact = composeTiledArena(name, "compact", maxWidth);
        if (compact != null && compact.fits(maxWidth, maxHeight)) {
            return compact;
        }

        return loadBest("arena", name, maxWidth, maxHeight);
    }

    public static AsciiSprite load(String category, String name, String variant) throws IOException {
        Path filePath = ROOT.resolve(category).resolve(name + "_" + variant + ".txt");
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        return new AsciiSprite(lines);
    }

    private static AsciiSprite composeTiledArena(String name, String variant, int targetWidth) throws IOException {
        List<AsciiSprite> tiles = loadArenaTiles(name, variant);
        if (tiles.isEmpty()) {
            return null;
        }

        int width = Math.max(1, targetWidth);
        int height = 0;
        for (AsciiSprite tile : tiles) {
            height = Math.max(height, tile.getHeight());
        }

        List<String> composedLines = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            StringBuilder lineBuilder = new StringBuilder();
            int tileIndex = 0;
            while (lineBuilder.length() < width) {
                AsciiSprite tile = tiles.get(tileIndex % tiles.size());
                String rowText = row < tile.getHeight() ? tile.getLines().get(row) : "";
                lineBuilder.append(padRight(rowText, tile.getWidth()));
                tileIndex++;
            }
            composedLines.add(lineBuilder.substring(0, width));
        }

        return new AsciiSprite(composedLines);
    }

    private static List<AsciiSprite> loadArenaTiles(String name, String variant) throws IOException {
        List<AsciiSprite> tiles = new ArrayList<>();
        for (int index = 1; index <= 8; index++) {
            Path tilePath = ROOT.resolve("arena").resolve(name + "_tile" + index + "_" + variant + ".txt");
            if (!Files.exists(tilePath)) {
                continue;
            }
            List<String> lines = Files.readAllLines(tilePath, StandardCharsets.UTF_8);
            tiles.add(new AsciiSprite(lines));
        }
        return tiles;
    }

    private static String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }
}
