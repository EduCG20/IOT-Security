package entities;

import java.util.ArrayList;
import java.util.List;

public class House {
    private int id;
    private String name;
    private String address;
    private int userId;
    private List<BaseSensor> sensors;
    
    public House() {
        this.sensors = new ArrayList<>();
    }
    
    public House(String name, String address, int userId) {
        this.name = name;
        this.address = address;
        this.userId = userId;
        this.sensors = new ArrayList<>();
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public List<BaseSensor> getSensors() { return sensors; }
    public void setSensors(List<BaseSensor> sensors) { this.sensors = sensors; }
    
    public void addSensor(BaseSensor sensor) {
        this.sensors.add(sensor);
    }
}