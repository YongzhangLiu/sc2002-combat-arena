package arena.ui.sprite;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class SpriteCatalog {
    private static final Path ROOT = Paths.get("assets", "sprites");
    private static final String RESOURCE_ROOT = "assets/sprites";

    private SpriteCatalog() {
    }

    public static AsciiSprite loadArenaStrip(String name, int maxWidth) throws IOException {
        AsciiSprite normal = composeTiledArena(name, "normal", maxWidth);
        if (normal != null) {
            return normal;
        }
        return load("arena", name, "normal");
    }

    public static AsciiSprite loadCloudStrip(int maxWidth) throws IOException {
        AsciiSprite clouds = composeTiledWithoutVariant("cloud", "cloud", maxWidth);
        if (clouds != null) {
            return clouds;
        }
        throw new IOException("No cloud tiles found under assets/sprites/cloud");
    }

    public static AsciiSprite load(String category, String name, String variant) throws IOException {
        Path relativePath = Paths.get(category, name + "_" + variant + ".txt");
        List<String> lines = readSpriteLines(relativePath);
        return new AsciiSprite(lines);
    }

    public static AsciiSprite loadExact(String category, String filename) throws IOException {
        Path relativePath = Paths.get(category, filename);
        List<String> lines = readSpriteLines(relativePath);
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
            Path relativePath = Paths.get("arena", name + "_tile" + index + "_" + variant + ".txt");
            List<String> lines = tryReadSpriteLines(relativePath);
            if (lines == null) {
                continue;
            }
            tiles.add(new AsciiSprite(lines));
        }
        return tiles;
    }

    private static AsciiSprite composeTiledWithoutVariant(String category, String name, int targetWidth) throws IOException {
        List<AsciiSprite> tiles = loadTilesWithoutVariant(category, name);
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

    private static List<AsciiSprite> loadTilesWithoutVariant(String category, String name) throws IOException {
        List<AsciiSprite> tiles = new ArrayList<>();
        for (int index = 1; index <= 8; index++) {
            Path relativePath = Paths.get(category, name + "_tile" + index + ".txt");
            List<String> lines = tryReadSpriteLines(relativePath);
            if (lines == null) {
                continue;
            }
            tiles.add(new AsciiSprite(lines));
        }
        return tiles;
    }

    private static List<String> tryReadSpriteLines(Path relativePath) {
        try {
            return readSpriteLines(relativePath);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static List<String> readSpriteLines(Path relativePath) throws IOException {
        String resourcePath = RESOURCE_ROOT + "/" + relativePath.toString().replace('\\', '/');
        InputStream inputStream = SpriteCatalog.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        }

        Path filePath = ROOT.resolve(relativePath);
        return Files.readAllLines(filePath, StandardCharsets.UTF_8);
    }

    private static String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }
}
