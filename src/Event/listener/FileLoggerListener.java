package Event.listener;

import Event.*;
import Event.EventListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class FileLoggerListener implements EventListener<Event> {
    private final String logFilePath;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private boolean enabled = true;

    public FileLoggerListener(String logFilePath) {
        this.logFilePath = logFilePath;

        // Créer le répertoire des logs s'il n'existe pas
        try {
            Path logDir = Paths.get(logFilePath).getParent();
            if (logDir != null && !Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la création du répertoire des logs : " + e.getMessage());
        }
    }
    @Override
    public void onEvent(Event event) {
        if (!enabled) return;

        String logEntry = formatLogEntry(event);
        writeToFile(logEntry);
    }

    @Override
    public String[] getEventTypes() {
        return new String[]{
                "NOTE_CREATED", "NOTE_UPDATED", "NOTE_DELETED",
                "BULLETIN_GENERATED",
                "USER_LOGIN", "USER_LOGOUT",
                "ETUDIANT_CREATED", "ENSEIGNANT_CREATED"
        };
    }

    @Override
    public String getName() {
        return "FileLoggerListener";
    }

    private String formatLogEntry(Event event) {
        return String.format("[%s] [%s] User=%s (%d) | %s | EventID=%s%n",
                event.getTimestamp().format(dtf),
                event.getEventType(),
                event.getUserName(),
                event.getUserId(),
                event.getDescription(),
                event.getEventId()
        );
    }

    private void writeToFile(String logEntry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'écriture dans le fichier de log : " + e.getMessage());
        }
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}
