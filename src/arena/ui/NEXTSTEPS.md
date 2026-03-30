## Refactors
1. Settle entry point: `UIEntryPoint`, with main and arg parsing. 
2. Remove ui/model/, use proper e`ngine/GameState` class.
3. Setup screens should call methods in `engine/GameInit`, to construct initial state
4. Use proper player action interface in `engine/PlayerAction`
5. Test victory & loss screens

## Interfacing
> Beyond this point, engine is required to work
6. Make sure UI renders enemies, player and everything based on `engine/GameState`, dynamically updated every game turn.
7. Call engine/GameInit.startGame(), this will run a battle loop (not implemented yet), and return an int with win/loss condition. 
8. After win / loss, give options to start new game (start new loop) or go back to main menu. 

- Game start flow: UI entry -> call `LanternaStartMenu` (rename to `StartMenu`) -> call screens -> update status via GameInit -> start game by calling `startGame()` -> `startGame()` runs game engine loop -> UI runs update loop
    - To confirm: async `startGame()` loop & UI update loop?
- Update flow: UI receive & update player action -> engine capture action & update `GameState`, set flag `boolean updateStatus` -> UI confirm flag is set -> UI update render
    - To confirm: better to poll `GameState` at intervals or only each turn? 
