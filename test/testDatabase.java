package test;

import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // Creiamo una connessione al database in memoria
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        DBConnection.setConnection(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Chiudiamo la connessione al termine di ogni test
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testCreateTable() throws Exception {
        // Creiamo una tabella di test
        String sql = "CREATE TABLE IF NOT EXISTS TestTable (id INTEGER PRIMARY KEY, name TEXT);";
        DBConnection.query(sql);

        // Verifichiamo se la tabella è stata creata
        ResultSet rs = connection.getMetaData().getTables(null, null, "TestTable", null);
        assertTrue(rs.next());
        rs.close();
    }

    @Test
    void testInsertAndQuery() throws Exception {
        // Creiamo una tabella e inseriamo un record
        String createTable = "CREATE TABLE IF NOT EXISTS TestTable (id INTEGER PRIMARY KEY, name TEXT);";
        String insert = "INSERT INTO TestTable (id, name) VALUES (1, 'John Doe');";
        DBConnection.query(createTable);
        DBConnection.query(insert);

        // Verifichiamo se il record è stato inserito
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM TestTable WHERE id = 1;");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("id"));
        assertEquals("John Doe", rs.getString("name"));
        rs.close();
    }
}
