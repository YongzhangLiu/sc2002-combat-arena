package arena.ui;

/**
 * Carries setup plus display flags chosen in the start menu (including Options),
 * so battle and endgame UI match the active session.
 */
public record GameStartRequest(GameSetup setup, boolean fullScreen, boolean asciiMode) {}
