package entities;


import interfaces.Sensor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public abstract class BaseSensor implements Sensor {
    protected int id;
    protected int houseId;
    protected String name;
    protected String location;
    protected boolean isActive;
    protected String sensorType;
    protected List<SensorSchedule> schedules;
    
    public BaseSensor() {
        this.isActive = true;
    }
    
    @Override
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getHouseId() { return houseId; }
    public void setHouseId(int houseId) { this.houseId = houseId; }
    
    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    @Override
    public boolean isActive() { return isActive; }
    @Override
    public void activate() { this.isActive = true; }
    @Override
    public void deactivate() { this.isActive = false; }
    
    @Override
    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }
    
    public List<SensorSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<SensorSchedule> schedules) { this.schedules = schedules; }
    
    public void addSchedule(SensorSchedule schedule) {
        this.schedules.add(schedule);
    }
    
    protected boolean isWithinSchedule(LocalDateTime dateTime) {
        if (schedules == null || schedules.isEmpty()) return true;
        
        LocalTime currentTime = dateTime.toLocalTime();
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        
        for (SensorSchedule schedule : schedules) {
            if (!schedule.isActive()) continue;
            
            boolean dayMatches = false;
            switch (dayOfWeek) {
                case 1: dayMatches = schedule.isMonday(); break;
                case 2: dayMatches = schedule.isTuesday(); break;
                case 3: dayMatches = schedule.isWednesday(); break;
                case 4: dayMatches = schedule.isThursday(); break;
                case 5: dayMatches = schedule.isFriday(); break;
                case 6: dayMatches = schedule.isSaturday(); break;
                case 7: dayMatches = schedule.isSunday(); break;
            }
            
            if (dayMatches && 
                !currentTime.isBefore(schedule.getStartTime()) && 
                !currentTime.isAfter(schedule.getEndTime())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public abstract boolean checkUnauthorizedAccess(LocalDateTime dateTime);
    
    @Override
    public abstract String getStatusMessage();

//En BaseSensor.java debe existir:
public void setActive(boolean active) {
 this.isActive = active;
}

}