# sc2002-combat-arena
SC2002 Group Assignment: Turn-Based Combat Arena


## Build Tool

> **Maven** (`pom.xml`) used as build tool.

- Dependency management (e.g. Lanterna)
- Compilation
- Runs selected app entry points via profiles
- Output to build folder `target/`

### Build and run
> **Important Note:** JDK version was changed to 26, please run `Ctrl+Shift+P` -> Java: Clean language server workspace then `mvn clean compile exec:java`
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
    - `java -jar target/sc2002-combat-arena-0.2.0-all.jar`

Notes:
- The packaged build targets Java 26, you need JDK 26.
- Run from the project root so `assets/` relative paths resolve correctly.

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

### Package Layout
- `arena.ui`
    - `GameUI`, `CLIGameUI`
- `arena.engine`
    - `BattleEngine`, `WaveManager`
- `arena.model.combatant`
    - `Combatant`, `Player`, `Enemy`, `Warrior`, `Wizard`, `Goblin`, `Wolf`
- `arena.model.action`
    - `Action`, `BasicAttack`, `Defend`, `UseItem`, `UseSpecialSkill`
- `arena.model.item`
    - `Item`, `Potion`, `PowerStone`, `SmokeBomb`
- `arena.model.effect`
    - `StatusEffect`, `StunEffect`, `DefendBuff`, `SmokeBombEffect`
- `arena.strategy`
    - `TurnOrderStrategy`, `SpeedBasedTurnOrder`, `EnemyActionStrategy`, `BasicEnemyActionStrategy`

```text
src/arena/
├── ui/
├── engine/
├── model/
│   ├── combatant/
│   ├── action/
│   ├── item/
│   └── effect/
└── strategy/
```


### UML diagram
- mermaid (via extension `Markdown Preview Mermaid Support`)

> [Note]
> I added a minimal UML diagram below (AI generated) for clarity. Actual logic is up for discussion.

```mermaid
classDiagram
direction LR

%% ===== UI =====
class GameUI {
    <<interface>>
    +showBattleState()
    +choosePlayerAction()
    +showResult()
}

class CLIGameUI
GameUI <|.. CLIGameUI

%% ===== ENGINE =====
class BattleEngine {
    -turnOrderStrategy: TurnOrderStrategy
    -enemyActionStrategy: EnemyActionStrategy
    -ui: GameUI
    +runBattle()
    +processRound()
    +checkEndCondition()
}

class WaveManager {
    +spawnInitialWave()
    +spawnBackupWaveIfNeeded()
}

BattleEngine --> WaveManager : manages
BattleEngine --> GameUI : uses

%% ===== STRATEGY =====
class TurnOrderStrategy {
    <<interface>>
    +determineOrder(combatants)
}

class SpeedBasedTurnOrder
TurnOrderStrategy <|.. SpeedBasedTurnOrder

class EnemyActionStrategy {
    <<interface>>
    +selectAction(enemy, player)
}

class BasicEnemyActionStrategy
EnemyActionStrategy <|.. BasicEnemyActionStrategy

BattleEngine --> TurnOrderStrategy : delegates to
BattleEngine --> EnemyActionStrategy : delegates to

%% ===== MODEL =====
class Combatant {
    <<abstract>>
    -name: String
    -hp: int
    -maxHp: int
    -attack: int
    -defense: int
    -speed: int
    +takeDamage(amount)
    +isAlive() bool
}

class Player {
    <<abstract>>
    -cooldown: int
    -inventory: List~Item~
}

class Enemy
class Warrior
class Wizard
class Goblin
class Wolf

Combatant <|-- Player
Combatant <|-- Enemy
Player <|-- Warrior
Player <|-- Wizard
Enemy <|-- Goblin
Enemy <|-- Wolf

class Action {
    <<interface>>
    +execute(actor, targets)
}

class BasicAttack
class Defend
class UseItem
class UseSpecialSkill
Action <|.. BasicAttack
Action <|.. Defend
Action <|.. UseItem
Action <|.. UseSpecialSkill

class Item {
    <<interface>>
    +use(user, targets)
}

class Potion
class PowerStone
class SmokeBomb
Item <|.. Potion
Item <|.. PowerStone
Item <|.. SmokeBomb

class StatusEffect {
    <<interface>>
    +apply(target)
    +tick()
    +isExpired() bool
}

class StunEffect
class DefendBuff
class SmokeBombEffect
StatusEffect <|.. StunEffect
StatusEffect <|.. DefendBuff
StatusEffect <|.. SmokeBombEffect

Combatant "1" --> "*" StatusEffect : has
Player "1" --> "*" Item : holds
Player --> Action : chooses
Enemy --> Action : performs
UseItem --> Item : consumes
UseSpecialSkill --> StatusEffect : may apply
BattleEngine --> Combatant : controls turns
```