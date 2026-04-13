package arena.engine.qte;

public class NoOpQtePolicy implements QtePolicy {
    @Override
    public QteResult resolveAttackQte() {
        return QteResult.NORMAL;
    }
}
