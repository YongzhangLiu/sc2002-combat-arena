# sc2002-combat-arena
SC2002 Group Assignment: Turn-Based Combat Arena


## Build Tool

> **Maven** (`pom.xml`) used as build tool.

- Dependency management (e.g. Lanterna)
- Compilation
- Runs selected app entry points via profiles
- Output to build folder `target/`

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

### Package executable JAR

- Build runnable JAR (dependencies included):
    - `mvn clean package`
- Generated artifact:
    - `target/sc2002-combat-arena-0.2.0-all.jar`
- Run packaged game:
    - **Linux/macOS**: `java -jar target/sc2002-combat-arena-0.2.0-all.jar`
    - **Windows**: `javaw -jar target/sc2002-combat-arena-0.2.0-all.jar`

Notes:
- The packaged build targets Java 26, you need JDK 26.
- Run from the project root so `assets/` relative paths resolve correctly.

#### Windows Specific Complications
- Windows falls back to `SwingTerminalFrame`
- No support for mouse clicking 
- To build & run:
    ```
    mvn clean compile
    mvn -q compile exec:exec -Dexec.executable="C:\Program Files\Java\jdk-26\bin\java.exe" -Dexec.args="-cp %classpath arena.GameApp" -Dexec.classpathScope=runtime
    ```
    (replace with your actual java executable path. Check using `mvn -version`.)

## Playing the Game
- The game updates dynamically on terminal screen sizechange. Screen flashing is normal.


## Workflow

### Class design
- `/ui`: CLI/TUI interface (display + input)
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

