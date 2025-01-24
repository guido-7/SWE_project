package src.managerdatabase;

import java.sql.*;

public class DBConnection {

    private static Connection conn;
    private static final String dbPath = "database/bigDB.db";

    public static void connect() {
        try {
            if(conn == null || conn.isClosed()){

            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to database");
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
            // Verifica se la connessione esiste e la restituisce
            if (conn != null && !conn.isClosed()) {
                return conn;

            }

            // Carica il driver JDBC per SQLite
            Class.forName("org.sqlite.JDBC");

            // Apre la connessione al database SQLite
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
            // Verifica se la connessione esiste e chiude la connessione
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Disconnected from database");
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

}