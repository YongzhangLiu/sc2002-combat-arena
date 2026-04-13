package arena.engine.controller;

public class AiTurnResult {
    private final String message;
    private final Integer damageToPlayer;

    public AiTurnResult(String message, Integer damageToPlayer) {
        this.message = message;
        this.damageToPlayer = damageToPlayer;
    }

    public String getMessage() {
        return message;
    }

    public Integer getDamageToPlayer() {
        return damageToPlayer;
    }
}
