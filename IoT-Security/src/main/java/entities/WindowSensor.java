package entities;

import java.time.LocalDateTime;

public class WindowSensor extends BaseSensor {
    private boolean isBroken;
    private boolean isOpen;
    
    public WindowSensor() {
        this.sensorType = "WINDOW";
        this.isBroken = false;
        this.isOpen = false;
    }
    
    public boolean isBroken() { return isBroken; }
    public void setBroken(boolean broken) { isBroken = broken; }
    
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    
    @Override
    public boolean checkUnauthorizedAccess(LocalDateTime dateTime) {
        return (isOpen || isBroken) && isActive() && isWithinSchedule(dateTime);
    }
    
    @Override
    public String getStatusMessage() {
        return String.format("Window %s at %s - Open: %s, Broken: %s (Active: %s)", 
            name, location, isOpen ? "YES" : "NO", isBroken ? "YES" : "NO", isActive ? "YES" : "NO");
    }
}