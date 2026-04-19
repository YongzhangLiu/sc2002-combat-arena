# sc2002-combat-arena
SC2002 Group Assignment: Turn-Based Combat Arena


## Build Tool

> **Maven** (`pom.xml`) used as build tool.
- Dependency management (Lanterna)
- Compilation
- Runs selected app entry points via profiles
- Output to build folder `target/`
- Package cross-platform dependency-included `jar` packages

### Build and run
> **Important Note:** JDK version requirement is 26 
- Build game:
    - `mvn compile`
- Run game: 
    - `mvn exec:java`
- Build UI-only profile (legacy, for testing only):
    - `mvn -Pui-only compile`
- Run UI-only menu app:
    - `mvn -Pui-only exec:java`

### Windows Specific Complications
- Windows falls back to `SwingTerminalFrame`
- No support for mouse clicking 
- To build & run from source:
    ```bash
    mvn clean compile
    mvn exec:java
    # and if exec:java does not work:
    mvn -q compile exec:exec -Dexec.executable="C:\Program Files\Java\jdk-26\bin\java.exe" -Dexec.args="-cp %classpath arena.GameApp" -Dexec.classpathScope=runtime
    ```
    (replace with your actual java executable path. Check using `mvn -version`.)
- For a easier time, just run the packaged `jar`

### Package executable JAR
- Build runnable JAR (dependencies included):
    - `mvn clean package`
- Generated artifact:
    - `target/sc2002-combat-arena-<version>-all.jar`
- Run packaged game:
    - **Linux/macOS**: `java -jar target/sc2002-combat-arena-<version>-all.jar`
    - **Windows**: `javaw -jar target/sc2002-combat-arena-<version>-all.jar`
Notes:
- The packaged build targets Java 26, you need JDK 26.
- If you're running the package without the `-all` suffix, run from the project root so `assets/` relative paths resolve correctly.


## src layout
- `/ui`: TUI interface (display + input)
- `/engine`: battle control and game state logic
    - round loop, turn execution, win/lose checks
    - wave/level progression and backup spawn timing
    - invokes strategies and domain actions
- `/model`: entities and related
    - combatants (player/enemy), actions, items, status effects
    - stats, cooldown state, inventory, effect durations
- `/strategy` - swappable decision policies
    - turn order strategy
    - enemy action selection strategy
    - no ownership of battle state or wave lifecycle

