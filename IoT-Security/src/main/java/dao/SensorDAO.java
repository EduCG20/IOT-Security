package dao;

import entities.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SensorDAO {
    
    public BaseSensor getSensorById(int id) {
        String sql = "SELECT * FROM sensors WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BaseSensor sensor = createSensorFromResultSet(rs);
                
                if (sensor != null) {
                    sensor.setSchedules(getSchedulesForSensor(sensor.getId()));
                }
                
                return sensor;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<BaseSensor> getSensorsByHouse(int houseId) {
        List<BaseSensor> sensors = new ArrayList<>();
        String sql = "SELECT * FROM sensors WHERE house_id = ?";
        
        try (Connection conn = DatabaseConnection.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, houseId);
            ResultSet rs = stmt.executeQuery();
            
            // 🔹 PRIMERA FASE: obtener sensores
            while (rs.next()) {
                BaseSensor sensor = createSensorFromResultSet(rs);
                if (sensor != null) {
                    sensors.add(sensor);
                }
            }
            
            // 🔹 SEGUNDA FASE: cargar schedules (FUERA del ResultSet)
            for (BaseSensor sensor : sensors) {
                sensor.setSchedules(getSchedulesForSensor(sensor.getId()));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensors;
    }
    
    private BaseSensor createSensorFromResultSet(ResultSet rs) throws SQLException {
        String sensorType = rs.getString("sensor_type");
        BaseSensor sensor = null;
        
        switch (sensorType) {
            case "DOOR":
                sensor = new DoorSensor();
                break;
            case "WINDOW":
                sensor = new WindowSensor();
                break;
            default:
                return null;
        }
        
        sensor.setId(rs.getInt("id"));
        sensor.setHouseId(rs.getInt("house_id"));
        sensor.setName(rs.getString("name"));
        sensor.setLocation(rs.getString("location"));
        sensor.setActive(rs.getBoolean("is_active"));
        sensor.setSensorType(sensorType);
        
        return sensor;
    }
    
    private List<SensorSchedule> getSchedulesForSensor(int sensorId) {
        List<SensorSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM sensor_schedules WHERE sensor_id = ?";
        
        try (Connection conn = DatabaseConnection.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sensorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SensorSchedule schedule = new SensorSchedule();
                
                schedule.setId(rs.getInt("id"));
                schedule.setSensorId(rs.getInt("sensor_id"));
                
                Time start = rs.getTime("start_time");
                Time end = rs.getTime("end_time");
                
                if (start != null) {
                    schedule.setStartTime(start.toLocalTime());
                }
                
                if (end != null) {
                    schedule.setEndTime(end.toLocalTime());
                }
                
                schedule.setActive(rs.getBoolean("is_active"));
                schedule.setMonday(rs.getBoolean("monday"));
                schedule.setTuesday(rs.getBoolean("tuesday"));
                schedule.setWednesday(rs.getBoolean("wednesday"));
                schedule.setThursday(rs.getBoolean("thursday"));
                schedule.setFriday(rs.getBoolean("friday"));
                schedule.setSaturday(rs.getBoolean("saturday"));
                schedule.setSunday(rs.getBoolean("sunday"));
                
                schedules.add(schedule);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }
    
    public boolean saveSensor(BaseSensor sensor) {
        String sql = "INSERT INTO sensors (house_id, name, location, sensor_type, is_active) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, sensor.getHouseId());
            stmt.setString(2, sensor.getName());
            stmt.setString(3, sensor.getLocation());
            stmt.setString(4, sensor.getSensorType());
            stmt.setBoolean(5, sensor.isActive());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    sensor.setId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}