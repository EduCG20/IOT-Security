package entities;

import entities.HouseDAO;
import entities.House;
import controller.AuthController;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;

public class HouseController {
    private HouseDAO houseDAO;
    private AuthController authController;
    
    public HouseController(AuthController authController) {
        this.houseDAO = new HouseDAO();
        this.authController = authController;
    }
    
    public House getHouse(int houseId) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return houseDAO.getHouseById(houseId);
    }
    
    // ✅ NUEVO MÉTODO: Obtener todas las casas de un usuario
    public List<House> getHousesByUser(int userId) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        List<House> houses = new ArrayList<>();
        String sql = "SELECT id, name, address, user_id FROM houses WHERE user_id = ? OR user_id IS NULL";
        
        try (java.sql.PreparedStatement pstmt = houseDAO.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
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
    
    // ✅ NUEVO MÉTODO: Obtener todas las casas (sin filtro)
    public List<House> getAllHouses() {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        List<House> houses = new ArrayList<>();
        String sql = "SELECT id, name, address, user_id FROM houses";
        
        try (java.sql.Statement stmt = houseDAO.getConnection().createStatement()) {
            java.sql.ResultSet rs = stmt.executeQuery(sql);
            
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
    
    // ✅ NUEVO MÉTODO: Agregar una nueva casa
    public boolean addHouse(String name, String address, int userId) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        String sql = "INSERT INTO houses (name, address, user_id) VALUES (?, ?, ?)";
        
        try (java.sql.PreparedStatement pstmt = houseDAO.getConnection().prepareStatement(sql)) {
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