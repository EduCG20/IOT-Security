package entities;

import java.time.LocalDateTime;

public class DoorSensor extends BaseSensor {
    private boolean isOpen;
    
    public DoorSensor() {
        this.sensorType = "DOOR";
        this.isOpen = false;
    }
    
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    
    @Override
    public boolean checkUnauthorizedAccess(LocalDateTime dateTime) {
        return isOpen && isActive() && isWithinSchedule(dateTime);
    }
    
    @Override
    public String getStatusMessage() {
        return String.format("Door %s at %s is %s (Active: %s)", 
            name, location, isOpen ? "OPEN" : "CLOSED", isActive ? "YES" : "NO");
    }
}