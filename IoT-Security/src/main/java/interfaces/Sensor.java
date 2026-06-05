package interfaces;

import java.time.LocalDateTime;

public interface Sensor {
    int getId();
    String getName();
    String getLocation();
    boolean isActive();
    void activate();
    void deactivate();
    String getSensorType();
    boolean checkUnauthorizedAccess(LocalDateTime dateTime);
    String getStatusMessage();
}