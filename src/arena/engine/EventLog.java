package arena.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Read-only event log for UI consumption.
 * Engine appends events; UI reads and renders.
 */
public class EventLog {
    private final List<CombatEvent> events;

    public EventLog() {
        this.events = new ArrayList<>();
    }

    public void log(CombatEvent event) {
        events.add(event);
    }

    public List<CombatEvent> getRecentEvents(int count) {
        int start = Math.max(0, events.size() - count);
        return new ArrayList<>(events.subList(start, events.size()));
    }

    public List<CombatEvent> getAllEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clear() {
        events.clear();
    }
}
