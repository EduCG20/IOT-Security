package entities;


import java.time.LocalDateTime;

public class SensorEvent {
    private int id;
    private int sensorId;
    private LocalDateTime eventTime;
    private String status;
    private String message;
    private boolean isUnauthorized;
    
    public SensorEvent() {
        this.eventTime = LocalDateTime.now();
    }
    
    public SensorEvent(int sensorId, String status, String message, boolean isUnauthorized) {
        this.sensorId = sensorId;
        this.eventTime = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.isUnauthorized = isUnauthorized;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSensorId() { return sensorId; }
    public void setSensorId(int sensorId) { this.sensorId = sensorId; }
    
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isUnauthorized() { return isUnauthorized; }
    public void setUnauthorized(boolean unauthorized) { isUnauthorized = unauthorized; }
}