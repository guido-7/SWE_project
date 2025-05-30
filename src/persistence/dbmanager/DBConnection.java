package src.persistence.dbmanager;

import java.sql.*;

public class DBConnection {
    private static Connection conn;
    private static String dbPath = "database/bigDB.db";

    private DBConnection(){}

    public static void connect() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                System.out.println("Connected to database");
                conn.prepareStatement("PRAGMA foreign_keys = ON").executeUpdate();
            }

        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Failed to connect to database");
            e.printStackTrace();
        }
    }

    public static Connection open_connection() {
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }

            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            //System.out.println("Connected to database");
            try (PreparedStatement stmt = conn.prepareStatement("PRAGMA foreign_keys = ON")) {
                stmt.executeUpdate();
            }

        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Failed to connect to database");
            e.printStackTrace();

        }
        return conn;
    }

    // For test only
    public static Connection open_connection(String dbPathTest) {
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }

            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPathTest);
            //System.out.println("Connected to database");
            conn.prepareStatement("PRAGMA foreign_keys = ON").executeUpdate();

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
                conn = null;
                System.out.println("Disconnected from database");
            }
            else {
                System.out.println("Already disconnected from database");
            }
        } catch (SQLException e) {
            System.out.println("Failed to disconnect from database");
            e.printStackTrace();
        }
    }

    public static void query(String sql) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Failed to execute query");
            e.printStackTrace();
        }
    }

    public static void changeDBPath(String dbPath) {
        DBConnection.dbPath = dbPath;
    }

}