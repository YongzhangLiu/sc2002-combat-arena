# Arena Implementation Checklist (Turn-Based, Event-Driven)

## Goal
Implement the main combat arena UI from `ARENA_LAYOUT_SPEC.md` with predictable milestones and minimal rework.

---

## Phase 1 — Data Contract & State Plumbing

### 1.1 Define UI snapshot model
Create immutable UI-facing models in `arena.ui` or `arena.ui.model`:
- `ArenaViewState`
  - `roundNumber`
  - `turnOwnerName`
  - `playerState`
  - `List<EnemyViewState> enemies`
  - `List<String> combatLog`
  - `String feedbackMessage`
  - `boolean victory`
  - `boolean defeat`
- `PlayerViewState`
- `EnemyViewState`
- `StatusIconViewState`

Checklist:
- [ ] Add classes
- [ ] Keep read-only from UI perspective
- [ ] Include only fields needed by rendering

### 1.2 Define command model (UI -> engine)
- `ArenaUiCommand` (sealed interface or enum + payload)
  - `SelectAction`
  - `SelectItem`
  - `SelectTarget`
  - `ConfirmAction`
  - `Cancel`

Checklist:
- [ ] Define command types
- [ ] Ensure commands are serializable/simple for testing

---

## Phase 2 — Arena Screen Skeleton

### 2.1 Add screen host class
Create `arena.ui.screen.ArenaBattleScreen`.

Required methods:
- `void initialize(...)`
- `void render(ArenaViewState state)`
- `Optional<ArenaUiCommand> pollCommand()`
- `void showOverlay(ArenaViewState endState)`
- `void close()`

Checklist:
- [ ] Wire terminal-size-aware bounds calculation
- [ ] Ensure no per-frame loop (event-driven only)
- [ ] Keep one focused control at a time

### 2.2 Add panel layout calculator
Create helper class `ArenaLayoutCalculator`:
- Input: terminal `TerminalSize`
- Output: panel rectangles/bounds for:
  - action panel
  - status panel
  - info panel
  - player panel
  - enemy panel

Checklist:
- [ ] Add min-size fallback behavior
- [ ] Recalculate on each render/update call

---

## Phase 3 — Section Renderers

### 3.1 Background renderer
Create `ArenaBackgroundRenderer`:
- Render full-canvas background tile strip (tiled to viewport)

Checklist:
- [ ] Draw first every update
- [ ] Handle sprite fallback if width constrained

### 3.2 Action panel renderer
Create `ActionPanelRenderer`:
- Action buttons
- Item sub-list when selected mode is item
- Focus marker

Checklist:
- [ ] Keyboard + mouse button support
- [ ] Emit command intents (not domain mutation)

### 3.3 Status panel renderer
Create `StatusPanelRenderer`:
- HP bars and numeric labels
- Status icon rows with timers/counts

Checklist:
- [ ] Player + enemies shown consistently
- [ ] Dead targets visually distinct

### 3.4 Info panel renderer
Create `InfoPanelRenderer`:
- Round/turn info
- Rolling combat log
- Feedback line

Checklist:
- [ ] Bounded log lines
- [ ] Feedback always visible/high priority

### 3.5 Player panel renderer
Create `PlayerPanelRenderer`:
- Player sprite
- Name + compact HP summary

Checklist:
- [ ] Sprite fallback text if constrained

### 3.6 Enemy panel renderer
Create `EnemyPanelRenderer`:
- Dynamic slot sizing: `panelWidth / aliveEnemyCount`
- For each alive enemy, render vertically (top to bottom):
  1. Target select button (marked if current target)
  2. Status effects row (abbreviated)
  3. HP bar + numeric label
  4. Sprite with rightmost-pixel truncation if needed

Helper class `EnemySpriteClipper`:
- Take full sprite (char[][] or Lines)
- Crop to fixed width, keeping leftmost columns
- Return clipped sprite for rendering

Checklist:
- [ ] Alive enemy count drives width allocation
- [ ] Dead enemies removed from calculation
- [ ] Sprite truncation smooth (no garbage pixels)
- [ ] Button text changes based on selection state
- [ ] Effects row remains readable at minimum widths
- [ ] Auto-retarget visual after death

---

## Phase 4 — Input Controller

### 4.1 Add interaction coordinator
Create `ArenaInteractionController`:
- Maintains current local selection:
  - selected action
  - selected item
  - selected target index
- Converts UI interactions into `ArenaUiCommand`

Checklist:
- [ ] No direct engine calls here
- [ ] Deterministic keyboard navigation
- [ ] Mouse click paths mirror keyboard actions

### 4.2 Overlay interaction
- Disable underlying panel inputs when victory/defeat overlay active
- Overlay buttons only

Checklist:
- [ ] Input lock confirmed
- [ ] Overlay close action emits proper command/callback

---

## Phase 5 — Engine Integration Boundary

### 5.1 Add adapter from engine state -> view state
Create `ArenaViewStateMapper`:
- Maps engine snapshots to `ArenaViewState`

Checklist:
- [ ] No UI logic in mapper
- [ ] Handle null/empty statuses safely

### 5.2 Hook entry point
Use `LanternaUiEntryPoint` callback path to handoff setup and launch arena workflow:
- Setup complete -> engine starts battle -> emits snapshot -> UI render update

Checklist:
- [ ] Event-driven render calls only
- [ ] No busy refresh loop

---

## Phase 6 — Validation & UX Polish

### 6.1 Functional checks
- [ ] Mouse clicks work for action and target buttons
- [ ] Keyboard fallback complete
- [ ] Target button marks current target
- [ ] Dead enemy cleared and no longer targetable
- [ ] Victory/defeat overlay blocks combat input

### 6.2 Layout checks
- [ ] Standard terminal (>= 100x36)
- [ ] Small terminal fallback
- [ ] Resize behavior stable

### 6.3 Build checks
- [ ] `mvn -q -Pui-only compile`
- [ ] `mvn -Pui-only exec:java`

---

## Suggested File Set

- `src/arena/ui/screen/ArenaBattleScreen.java`
- `src/arena/ui/screen/arena/ArenaLayoutCalculator.java`
- `src/arena/ui/screen/arena/ArenaBackgroundRenderer.java`
- `src/arena/ui/screen/arena/ActionPanelRenderer.java`
- `src/arena/ui/screen/arena/StatusPanelRenderer.java`
- `src/arena/ui/screen/arena/InfoPanelRenderer.java`
- `src/arena/ui/screen/arena/PlayerPanelRenderer.java`
- `src/arena/ui/screen/arena/EnemyPanelRenderer.java`
- `src/arena/ui/screen/arena/ArenaInteractionController.java`
- `src/arena/ui/model/ArenaViewState.java`
- `src/arena/ui/model/PlayerViewState.java`
- `src/arena/ui/model/EnemyViewState.java`
- `src/arena/ui/model/StatusIconViewState.java`
- `src/arena/ui/model/ArenaUiCommand.java`
- `src/arena/ui/mapper/ArenaViewStateMapper.java`

---

## Recommended Build Order
1. View models + command model
2. Arena layout calculator
3. Arena screen shell
4. Info + player + enemy renderers
5. Action + status renderers
6. Interaction controller
7. Overlay logic
8. Engine mapper + entrypoint wiring
9. QA checks and cleanup
