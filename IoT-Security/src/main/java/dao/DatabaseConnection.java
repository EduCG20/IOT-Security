package dao;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/iot_security";
    private static final String USER = "root";
    private static final String PASSWORD = "snk2001";
    
    private static Connection conexion = null;
    
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("org.mariadb.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conexion;
    }
}