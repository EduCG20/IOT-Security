package entities;

import dao.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HouseDAO {
    
    // ✅ Método para obtener la conexión
    public Connection getConnection() {
        return DatabaseConnection.getConexion();
    }
    
    public House getHouseById(int houseId) {
        String sql = "SELECT id, name, address, user_id FROM houses WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, houseId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                House house = new House();
                house.setId(rs.getInt("id"));
                house.setName(rs.getString("name"));
                house.setAddress(rs.getString("address"));
                house.setUserId(rs.getInt("user_id"));
                return house;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ✅ Método para obtener casas por usuario
    public List<House> getHousesByUser(int userId) {
        List<House> houses = new ArrayList<>();
        String sql = "SELECT id, name, address, user_id FROM houses WHERE user_id = ? OR user_id IS NULL";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                House house = new House();
                house.setId(rs.getInt("id"));
                house.setName(rs.getString("name"));
                house.setAddress(rs.getString("address"));
                house.setUserId(rs.getInt("user_id"));
                houses.add(house);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return houses;
    }
    
    // ✅ Método para obtener todas las casas
    public List<House> getAllHouses() {
        List<House> houses = new ArrayList<>();
        String sql = "SELECT id, name, address, user_id FROM houses";
        
        try (Statement stmt = getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                House house = new House();
                house.setId(rs.getInt("id"));
                house.setName(rs.getString("name"));
                house.setAddress(rs.getString("address"));
                house.setUserId(rs.getInt("user_id"));
                houses.add(house);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return houses;
    }
    
    // ✅ Método para agregar una casa
    public boolean addHouse(String name, String address, int userId) {
        String sql = "INSERT INTO houses (name, address, user_id) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}