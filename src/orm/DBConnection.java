package src.orm;

import java.sql.*;
/*
public class DBConnection {

    private static Connection conn;
    private static final String dbPath = "database/bigDB.db";

    public static Connection connect() {
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }

            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to database");

        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Failed to connect to database");
            e.printStackTrace();

        }
        return conn;
    }

    public static void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Disconnected from database");
            }
        } catch (SQLException e) {
            System.out.println("Failed to disconnect from database");
            e.printStackTrace();
        }
    }

}*/