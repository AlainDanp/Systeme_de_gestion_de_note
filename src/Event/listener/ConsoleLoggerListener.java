package Event.listener;

import Event.EventListener;
import Event.Event;

import java.time.format.DateTimeFormatter;

public class ConsoleLoggerListener implements EventListener<Event> {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private boolean enabled = true;

    @Override
    public void onEvent(Event event) {
        if (!enabled) return;

        String timestamp = event.getTimestamp().format(dtf);
        String icon = getIconForEventType(event.getEventType());

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.printf(" ║ %s [%s] %s%n", icon, event.getEventType(), " ".repeat(40));
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf(" ║   Date      : %-47s║%n", timestamp);
        System.out.printf(" ║   Utilisateur: %-46s║%n", event.getUserName());
        System.out.printf(" ║   Description: %-46s║%n", event.getDescription());
        System.out.printf(" ║   Event ID  : %-47s║%n", event.getEventId().substring(0, 8) + "...");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
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
        return "ConsoleLoggerListener";
    }

    private String getIconForEventType(String eventType) {
        return switch (eventType) {
            case "NOTE_CREATED", "BULLETIN_GENERATED", "ETUDIANT_CREATED", "ENSEIGNANT_CREATED" -> "✅";
            case "NOTE_UPDATED" -> "✏️";
            case "NOTE_DELETED" -> "🗑️";
            case "USER_LOGIN" -> "🔓";
            case "USER_LOGOUT" -> "🔒";
            default -> "📌";
        };
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
