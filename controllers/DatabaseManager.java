package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    
    // database information
    private static final String DB_URL = "jdbc:mysql://localhost:3306/crescendo";
    private static final String USER = "root";
    private static final String PASS = "Ege754";

    /**
     * Establishes connection to database and returns it
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}