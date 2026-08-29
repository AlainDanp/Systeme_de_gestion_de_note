package MVC;

import java.time.LocalDateTime;

public class Notification {

    public enum Type{
        INFO, SUCCESS, WARNING, ERROR
    }
    private Integer id;
    private String message;
    private Type type;
    private Integer userId;
    private String eventType;
    private String eventId;
    private boolean lu;
    private LocalDateTime createdAt;

    public Notification() {
        this.lu = false;
        this.createdAt = LocalDateTime.now();
    }
    public Notification(String message, Type type, Integer userId, String eventType, String eventId) {
        this();
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.eventType = eventType;
        this.eventId = eventId;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getIcon() {
        return switch (type) {
            case INFO -> "ℹ️";
            case SUCCESS -> "✅";
            case WARNING -> "⚠️";
            case ERROR -> "❌";
        };
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s", getIcon(), type, message);
    }

}
