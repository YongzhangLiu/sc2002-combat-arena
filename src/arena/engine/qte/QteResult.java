package arena.engine.qte;

public enum QteResult {
    EARLY(0.5),
    NORMAL(1.0),
    PERFECT(2.0);

    private final double multiplier;

    QteResult(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
