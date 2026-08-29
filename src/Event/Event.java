package Event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Event {
    String getEventId();
    String getEventType();
    LocalDateTime getTimestamp();
    Integer getUserId();
    String getUserName();
    String getDescription();
}
