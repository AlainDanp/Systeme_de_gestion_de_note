package Event;

public interface EventListener <T extends Event>{
    void onEvent(T event);
    String[] getEventTypes();
    String getName();

}
