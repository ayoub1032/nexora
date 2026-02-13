package tn.nexora.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static MyConnection instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306/nexora";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database!");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
