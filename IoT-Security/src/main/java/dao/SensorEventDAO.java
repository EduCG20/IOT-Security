package dao;

import entities.SensorEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SensorEventDAO {
    
    public boolean createEvent(SensorEvent event) {
        String sql = "INSERT INTO sensor_events (sensor_id, status, message, is_unauthorized) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, event.getSensorId());
            stmt.setString(2, event.getStatus());
            stmt.setString(3, event.getMessage());
            stmt.setBoolean(4, event.isUnauthorized());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<SensorEvent> getEventsBySensor(int sensorId) {
        List<SensorEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM sensor_events WHERE sensor_id = ? ORDER BY event_time DESC";
        
        try (Connection conn = DatabaseConnection.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sensorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SensorEvent event = new SensorEvent();
                event.setId(rs.getInt("id"));
                event.setSensorId(rs.getInt("sensor_id"));
                event.setEventTime(rs.getTimestamp("event_time").toLocalDateTime());
                event.setStatus(rs.getString("status"));
                event.setMessage(rs.getString("message"));
                event.setUnauthorized(rs.getBoolean("is_unauthorized"));
                
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
}