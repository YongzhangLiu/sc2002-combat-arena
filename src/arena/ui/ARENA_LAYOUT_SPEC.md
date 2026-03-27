# Arena Layout Spec (Turn-Based)

## Scope
This document defines the combat arena UI layout and update model for the Lanterna-based turn system.

Because combat is turn-based, UI updates are **event-driven**, not continuous frame rendering.

---

## Rendering Model

### Update Trigger
Render the arena only when one of these occurs:
- Engine emits a new battle state snapshot
- Player changes selection (action/item/target)
- Overlay state changes (victory/defeat)
- Validation/feedback message changes

### Render Sequence (per update)
1. Background layer (full-canvas arena tiles)
2. Panel borders + static section headers
3. Dynamic section content (bars, icons, sprites, logs)
4. Selection/focus markers (target, active action)
5. Overlay (victory/defeat), if active

No periodic redraw loop is required.

---

## Layout Regions

### 1) Top Left — Action Panel
- Purpose: action selection, skill/action list, item list (when item mode active)
- Border: yes
- Input: keyboard + mouse button clicks

### 2) Top Right — Status Panel
- Upper: HP/status bars (player + enemies)
- Lower: status icons and timers (buff/debuff/cooldown)
- Border: yes

### 3) Bottom Left — Info + Player
Two blocks in bottom-left zone:
- **Info Panel (left)**
  - Border: yes
  - Contains:
    - Round / turn info
    - Combat log (rolling list)
    - Feedback line (validation/action result)
- **Player Panel (right)**
  - Player sprite + name (+ optional compact HP)

### 4) Bottom Right — Enemy Panel
- Enemy slots/cards laid out left-to-right
- Border: yes
- Dead enemies are cleared from active display and targeting

#### Enemy Slot Layout (top to bottom)
Each enemy occupies a fixed horizontal slice: `panelWidth / aliveEnemyCount`

1. **Target Select Button** (top)
   - Single button per enemy
   - Marked when selected (e.g., `[SELECTED]` vs `SELECT`)
   - Keyboard + mouse click support

2. **Status Effects Row**
   - Abbreviated effect names or icons
   - Centered in slot

3. **HP Bar + Label**
   - Format: `████ 45/100` (bar + numeric)
   - Truncated horizontally to fit slot

4. **Sprite** (bottom)
   - Rendered at full height (remaining vertical space)
   - **Sprite Truncation**: If sprite width > slot width, crop rightmost pixels
   - Fallback text if sprite unavailable
   - Visually centers within available space

#### Dead Enemy Handling
- Remove from alive count immediately
- Width distribution recalculates on next render
- Dead enemies no longer consume slot space

### 5) Background Tiles
- Arena tiles are rendered as a full-canvas background layer
- Not a footer strip
- Foreground panels are rendered above this layer

---

## Targeting Rules
- Default target: first alive enemy at start of player turn
- Target can be changed via:
  - small select button above each enemy
  - keyboard target cycle keys (if defined)
- If selected target dies: auto-select next alive enemy
- If no alive enemies remain: trigger victory overlay

---

## Dead Enemy Handling
On enemy death:
- Set HP/status to dead state in status panel
- Remove from targetable list immediately
- Remove/clear enemy slot from active sprite area on next render update

---

## Turn Info / Log / Feedback
Info panel should include:
- `Round X | Turn: <unit>`
- Rolling combat log (bounded visible lines)
- Feedback line for immediate messages:
  - invalid action
  - cooldown blocked
  - no item available
  - action result summary

---

## Victory / Defeat Overlay
- Render as modal overlay on top of combat layout
- Battlefield remains visible underneath (frozen state)
- Disable underlying combat inputs while overlay is active
- Show:
  - Result title (Victory/Defeat)
  - Summary stats
  - Next actions (continue/menu/restart as implemented)

---

## Suggested Data Contract (Engine -> UI)
Each emitted snapshot should include:
- Round number
- Current turn owner
- Player state (HP/maxHP, statuses, sprite key)
- Enemy list (alive/dead, HP/maxHP, statuses, sprite key, targetable)
- Action availability/cooldowns
- Recent combat events (for log)
- End-state flags (victory/defeat)

---

## Notes
- Design for terminal resize by recalculating panel bounds on each update.
- Maintain one focused control at a time.
- Keep visual style consistent with existing setup screens (bordered panels, centered labels, compact fallback behavior).
