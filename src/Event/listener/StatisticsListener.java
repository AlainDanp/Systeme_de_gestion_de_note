package Event.listener;

import Event.Event;
import Event.EventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsListener implements EventListener<Event> {
    private final Map<String, AtomicInteger > eventCounts;
    private final Map<Integer, AtomicInteger> userEventCounts;
    private final Map<String, Event> lastEventByType;

    public StatisticsListener() {
        this.eventCounts = new ConcurrentHashMap<>();
        this.userEventCounts = new ConcurrentHashMap<>();
        this.lastEventByType = new ConcurrentHashMap<>();
    }
    @Override
    public void onEvent(Event event) {
        // Compter par type
        eventCounts.computeIfAbsent(event.getEventType(), k -> new AtomicInteger(0))
                .incrementAndGet();

        // Compter par utilisateur
        userEventCounts.computeIfAbsent(event.getUserId(), k -> new AtomicInteger(0))
                .incrementAndGet();

        // Enregistrer le dernier événement de ce type
        lastEventByType.put(event.getEventType(), event);
    }

    @Override
    public String[] getEventTypes() {
        return new String[]{
                "NOTE_CREATED", "NOTE_UPDATED", "NOTE_DELETED",
                "BULLETIN_GENERATED",
                "USER_LOGIN", "USER_LOGOUT",
                "ETUDIANT_CREATED", "ENSEIGNANT_CREATED",
                "MATIERE_ENSEIGNANT_MODIFIE"
        };
    }

    @Override
    public String getName() {
        return "StatisticsListener";
    }

    public int getEventCount(String eventType) {
        AtomicInteger count = eventCounts.get(eventType);
        return count != null ? count.get() : 0;
    }

    public Map<String, Integer> getAllEventCounts() {
        Map<String, Integer> result = new HashMap<>();
        eventCounts.forEach((type, count) -> result.put(type, count.get()));
        return result;
    }

    public int getTotalEventCount() {
        return eventCounts.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    public int getUserEventCount(Integer userId) {
        AtomicInteger count = userEventCounts.get(userId);
        return count != null ? count.get() : 0;
    }
    public Map<Integer, Integer> getAllUserEventCounts() {
        Map<Integer, Integer> result = new HashMap<>();
        userEventCounts.forEach((userId, count) -> result.put(userId, count.get()));
        return result;
    }

    public Event getLastEvent(String eventType) {
        return lastEventByType.get(eventType);
    }

    public void reset() {
        eventCounts.clear();
        userEventCounts.clear();
        lastEventByType.clear();
    }

    public void printStatistics() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    STATISTIQUES DES ÉVÉNEMENTS                 ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf(" ║   Total d'événements : %-40d║%n", getTotalEventCount());
        System.out.println("╠════════════════════════════════════════════════════════════════╣");

        getAllEventCounts().forEach((type, count) -> {
            System.out.printf("║  %-30s : %-28d║%n", type, count);
        });

        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }
}
