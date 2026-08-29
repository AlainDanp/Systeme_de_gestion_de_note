package Event;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseEvent implements Event {

    private final String eventId;
    private final LocalDateTime timestamp;
    private final Integer userId;
    private final String userName;

    public BaseEvent(Integer userId, String userName) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    @Override
    public String getUserName() {
        return userName;
    }
    @Override
    public String toString() {
        return String.format("[%s] %s by %s at %s",
                getEventType(), getDescription(), userName, timestamp);
    }

}
