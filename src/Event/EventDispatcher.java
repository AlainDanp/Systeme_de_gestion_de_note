package Event;

import java.util.*;
import java.util.concurrent.*;

public class EventDispatcher {
    private static EventDispatcher instance;
    private final Map<String, List<EventListener<? extends Event>>> listeners;
    private final ExecutorService executorService;
    private final Queue<Event> eventQueue;

    private final List<Event> eventHistory;
    private final int maxHistorySize = 100;

    private EventDispatcher() {
        this.listeners = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(5);
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.eventHistory = Collections.synchronizedList(new ArrayList<>());
    }

    public static synchronized EventDispatcher getInstance() {
        if (instance == null) {
            instance = new EventDispatcher();
        }
        return instance;
    }

    public <T extends Event> void registerListener(EventListener<T> listener) {
        for (String eventType : listener.getEventTypes()) {
            listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        }
        System.out.println(" Listener enregistré: " + listener.getName() +
                " pour " + Arrays.toString(listener.getEventTypes()));
    }

    public <T extends Event> void unregisterListener(EventListener<T> listener) {
        for (String eventType : listener.getEventTypes()) {
            List<EventListener<? extends Event>> list = listeners.get(eventType);
            if (list != null) {
                list.remove(listener);
            }
        }
        System.out.println(" Listener désinscrit: " + listener.getName());
    }

    public void dispatch(Event event) {
        addToHistory(event);
        List<EventListener<? extends Event>> eventListeners =
                listeners.getOrDefault(event.getEventType(), Collections.emptyList());

        for (EventListener listener : eventListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                System.err.println(" Erreur dans le Event.listener " + listener.getName() +
                        ": " + e.getMessage());
            }
        }
    }

    public void dispatchAsync(Event event) {
        executorService.submit(() -> dispatch(event));
    }

    public void enqueue(Event event) {
        eventQueue.offer(event);
    }

    public void processQueue() {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            if (event != null) {
                dispatch(event);
            }
        }
    }

    private void addToHistory(Event event) {
        synchronized (eventHistory) {
            eventHistory.add(event);
            if (eventHistory.size() > maxHistorySize) {
                eventHistory.remove(0);
            }
        }
    }

    public List<Event> getHistory() {
        return new ArrayList<>(eventHistory);
    }

    public List<Event> getHistoryByType(String eventType) {
        synchronized (eventHistory) {
            return eventHistory.stream()
                    .filter(e -> e.getEventType().equals(eventType))
                    .toList();
        }
    }

    public List<Event> getHistoryByUser(Integer userId) {
        synchronized (eventHistory) {
            return eventHistory.stream()
                    .filter(e -> e.getUserId().equals(userId))
                    .toList();
        }
    }

    public void clearHistory() {
        eventHistory.clear();
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        synchronized (eventHistory) {
            for (Event event : eventHistory) {
                stats.merge(event.getEventType(), 1, Integer::sum);
            }
        }
        return stats;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        System.out.println("EventDispatcher arrêté");
    }

}
