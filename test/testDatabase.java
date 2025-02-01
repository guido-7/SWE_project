package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class SetDBTest {

    private Connection conn;

    @BeforeEach
    void setUp() {
        conn = DBConnection.open_connection("database/bigDBTest.db");
        SetDB.createDB();

    }

    @AfterEach
    void tearDown() {
        DBConnection.disconnect();
    }

    @Test
    void testCreateUser() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='User';");
        assertTrue(rs.next(), "La tabella User non è stata creata correttamente.");
    }

    @Test
    void testInsertUser() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO User (nickname, name, surname) VALUES ('giorgio89', 'giorgio', 'rossi');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM User WHERE nickname = 'giorgio89';");
        assertTrue(rs.next(), "L'utente non è stato inserito correttamente.");
        assertEquals("giorgio", rs.getString("name"), "Il nome dell'utente non corrisponde.");
        assertEquals("rossi", rs.getString("surname"), "Il cognome dell'utente non corrisponde.");
    }

    @Test
    void testCreateCommunity() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Community';");
        assertTrue(rs.next(), "La tabella Community non è stata creata correttamente.");
    }

    @Test
    void testInsertCommunity() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Community (title, description) VALUES ('Tech Community', 'Una community dedicata alla tecnologia');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Community WHERE title = 'Tech Community';");
        assertTrue(rs.next(), "La community non è stata inserita correttamente.");
        assertEquals("Tech Community", rs.getString("title"), "Il nome della community non corrisponde.");
        assertEquals("Una community dedicata alla tecnologia", rs.getString("description"), "La descrizione della community non corrisponde.");
    }

    @Test
    void testCreateUserAccess() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='UserAccess';");
        assertTrue(rs.next(), "La tabella UserAccess non è stata creata correttamente.");
    }

    @Test
    void testInsertUserAccess() throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("INSERT INTO UserAccess (email, user_id, password, authen) VALUES ('test@gmail.com', 1, 'Password123', 1);");
        stmt.executeUpdate("INSERT INTO UserAccess (email, user_id, password, authen) VALUES ('test2@gmail.com', 2, 'Password345', 0);");

        ResultSet rs1 = stmt.executeQuery("SELECT * FROM UserAccess WHERE email = 'test@gmail.com';");
        assertTrue(rs1.next(), "I dati del primo utente non sono stati inseriti correttamente.");
        assertEquals("test@gmail.com", rs1.getString("email"), "L'email del primo utente non corrisponde.");
        assertEquals(1, rs1.getInt("user_id"), "L'user_id del primo utente non corrisponde.");
        assertEquals("Password123", rs1.getString("password"), "La password del primo utente non corrisponde.");
        assertEquals(1, rs1.getInt("authen"), "Il valore di 'authen' del primo utente non corrisponde.");

        ResultSet rs2 = stmt.executeQuery("SELECT * FROM UserAccess WHERE email = 'test2@gmail.com';");
        assertTrue(rs2.next(), "I dati del secondo utente non sono stati inseriti correttamente.");
        assertEquals("test2@gmail.com", rs2.getString("email"), "L'email del secondo utente non corrisponde.");
        assertEquals(2, rs2.getInt("user_id"), "L'user_id del secondo utente non corrisponde.");
        assertEquals("Password345", rs2.getString("password"), "La password del secondo utente non corrisponde.");
        assertEquals(0, rs2.getInt("authen"), "Il valore di 'authen' del secondo utente non corrisponde.");
    }


    @Test
    void testCreateRules() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Rules';");
        assertTrue(rs.next(), "La tabella Rules non è stata creata correttamente.");
    }

    @Test
    void testInsertRules() throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("INSERT INTO Rules (id, community_id, content) VALUES (1, 1, 'Regola 1');");
        stmt.executeUpdate("INSERT INTO Rules (id, community_id, content) VALUES (2, 1, 'Regola 2');");

        ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rules WHERE content = 'Regola 1';");
        assertTrue(rs1.next(), "La regola 'Regola 1' non è stata inserita correttamente.");
        assertEquals("Regola 1", rs1.getString("content"), "Il contenuto della regola non corrisponde.");
        assertEquals(1, rs1.getInt("community_id"), "L'ID della community non corrisponde.");

        ResultSet rs2 = stmt.executeQuery("SELECT * FROM Rules WHERE content = 'Regola 2';");
        assertTrue(rs2.next(), "La regola 'Regola 2' non è stata inserita correttamente.");
        assertEquals("Regola 2", rs2.getString("content"), "Il contenuto della regola non corrisponde.");
        assertEquals(1, rs2.getInt("community_id"), "L'ID della community non corrisponde.");
    }

    @Test
    void testRulesTrigger() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM Rules;");

        // Inserisce due regole nella tabella 'Rules'
        stmt.executeUpdate("INSERT INTO Rules (community_id, content) VALUES (2, 'Regola 1 test');");
        stmt.executeUpdate("INSERT INTO Rules (community_id, content) VALUES (2, 'Regola 2 test');");

        // Verifica che l'ID della seconda regola sia 2 (auto-incremento)
        ResultSet rs = stmt.executeQuery("SELECT id, content FROM Rules WHERE content = 'Regola 2 test';");
        assertTrue(rs.next(), "La regola 'Regola 2' non è stata inserita correttamente.");
        assertEquals(2, rs.getInt("id"), "L'ID della seconda regola non è stato incrementato correttamente.");
        assertEquals("Regola 2 test", rs.getString("content"), "Il contenuto della regola non corrisponde.");
    }

    @Test
    void testCreatePost() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Post';");
        assertTrue(rs.next(), "La tabella Post non è stata creata correttamente.");
    }

    @Test
    void testInsertPost() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Post (time, content, user_id, community_id) VALUES ('2025-01-29 12:00:00', 'Contenuto del post 1', 1, 1);");
        stmt.executeUpdate("INSERT INTO Post (time, content, user_id, community_id) VALUES ('2025-01-29 14:00:00', 'Contenuto del post 2', 2, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Post WHERE user_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato il post per user_id = 1.");
        assertEquals("2025-01-29 12:00:00", rs.getString("time"), "L'orario inserito non corrisponde.");
        assertEquals("Contenuto del post 1", rs.getString("content"), "Il contenuto del post non corrisponde.");
        assertEquals(1, rs.getInt("user_id"), "L'user_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("community_id"), "Il community_id inserito non corrisponde.");

        rs = stmt.executeQuery("SELECT * FROM Post WHERE user_id = 2;");
        assertTrue(rs.next(), "Non è stato trovato il post per user_id = 2.");
        assertEquals("2025-01-29 14:00:00", rs.getString("time"), "L'orario inserito non corrisponde.");
        assertEquals("Contenuto del post 2", rs.getString("content"), "Il contenuto del post non corrisponde.");
        assertEquals(2, rs.getInt("user_id"), "L'user_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("community_id"), "Il community_id inserito non corrisponde.");
    }

    @Test
    void testCreateComment() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Comment';");
        assertTrue(rs.next(), "La tabella Comment non è stata creata correttamente.");
    }

    @Test
    void testInsertComment() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Comment (post_id, level, user_id, content) VALUES (1, 0, 1, 'Contenuto del commento 1');");
        stmt.executeUpdate("INSERT INTO Comment (post_id, level, user_id, content) VALUES (1, 0, 2, 'Contenuto del commento 2');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Comment WHERE user_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato il commento per user_id = 1.");
        assertEquals(1, rs.getInt("post_id"), "Il post_id inserito non corrisponde.");
        assertEquals(0, rs.getInt("level"), "Il livello inserito non corrisponde.");
        assertEquals("Contenuto del commento 1", rs.getString("content"), "Il contenuto del commento non corrisponde.");

        rs = stmt.executeQuery("SELECT * FROM Comment WHERE user_id = 2;");
        assertTrue(rs.next(), "Non è stato trovato il commento per user_id = 2.");
        assertEquals(1, rs.getInt("post_id"), "Il post_id inserito non corrisponde.");
        assertEquals(0, rs.getInt("level"), "Il livello inserito non corrisponde.");
        assertEquals("Contenuto del commento 2", rs.getString("content"), "Il contenuto del commento non corrisponde.");
    }

    @Test
    void testCommentTrigger() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM Comment;");
        stmt.executeUpdate("INSERT INTO Post (time, content, user_id, community_id) VALUES ('2023-10-01', 'Post di test', 1, 1);");
        stmt.executeUpdate("INSERT INTO Comment (post_id, user_id, content) VALUES (1, 1, 'Commento 1');");
        stmt.executeUpdate("INSERT INTO Comment (post_id, user_id, content) VALUES (1, 1, 'Commento 2');");

        ResultSet rs = stmt.executeQuery("SELECT id FROM Comment WHERE content = 'Commento 2';");
        assertTrue(rs.next(), "Il trigger per l'auto-incremento non funziona correttamente.");
        assertEquals(2, rs.getInt("id"), "L'ID del commento non è stato incrementato correttamente.");
    }

    //todo testCommentWithoutPost da finire...
//    @Test
//    void testCommentWithoutPost() throws SQLException {
//        Statement stmt = conn.createStatement();
//        stmt.executeUpdate("DELETE FROM Comment;");
//        stmt.executeUpdate("PRAGMA foreign_keys = ON;");
//        stmt.executeUpdate("INSERT INTO Comment (post_id, user_id, content) VALUES (100, 1, 'Commento senza post');");
//
//        ResultSet rs = stmt.executeQuery("SELECT * FROM Comment WHERE post_id = 100;");
//        assertEquals(0, rs.getInt("id"), "Il commento non dovrebbe essere stato inserito.");
//    }

    @Test
    void testCreateCommentHierarchy() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CommentHierarchy';");
        assertTrue(rs.next(), "La tabella 'CommentHierarchy' non è stata creata correttamente.");
    }

    @Test
    void testInsertCommentHierarchy() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO CommentHierarchy (post_id, parent_id, child_id) VALUES (1, 1, 2);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM CommentHierarchy WHERE post_id = 1 AND parent_id = 1;");
        assertTrue(rs.next(), "Non è stata trovata la gerarchia per post_id = 1.");
        assertEquals(1, rs.getInt("post_id"), "Il post_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("parent_id"), "Il parent_id inserito non corrisponde.");
        assertEquals(2, rs.getInt("child_id"), "Il child_id inserito non corrisponde.");
    }

    @Test
    void testCreateBannedUsers() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='BannedUsers';");
        assertTrue(rs.next(), "La tabella 'BannedUsers' non è stata creata correttamente.");
    }

    @Test
    void testInsertBannedUsers() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO BannedUsers (user_id, community_id, ban_date, reason) VALUES (1, 1, '2025-01-29', 'Motivo del ban');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM BannedUsers WHERE user_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato l'utente bannato con user_id = 1.");
        assertEquals(1, rs.getInt("user_id"), "L'user_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("community_id"), "Il community_id inserito non corrisponde.");
        assertEquals("2025-01-29", rs.getString("ban_date"), "La data di ban inserita non corrisponde.");
        assertEquals("Motivo del ban", rs.getString("reason"), "Il motivo del ban non corrisponde.");
    }

    @Test
    void testCreateModerator() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Moderator';");
        assertTrue(rs.next(), "La tabella 'Moderator' non è stata creata correttamente.");
    }

    @Test
    void testInsertModerator() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Moderator (user_id, community_id, assigned_date) VALUES (1, 1, '2025-01-29');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Moderator WHERE user_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato il moderatore con user_id = 1.");
        assertEquals(1, rs.getInt("user_id"), "L'user_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("community_id"), "Il community_id inserito non corrisponde.");
        assertEquals("2025-01-29", rs.getString("assigned_date"), "La data di assegnazione non corrisponde.");
    }

    @Test
    void testCreateSubscription() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Subscription';");
        assertTrue(rs.next(), "La tabella 'Subscription' non è stata creata correttamente.");
    }

    @Test
    void testInsertSubscription() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Subscription (user_id, community_id, subscription_date) VALUES (1, 1, '2025-01-29');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Subscription WHERE user_id = 1;");
        assertTrue(rs.next(), "Non è stata trovata la sottoscrizione con user_id = 1.");
        assertEquals(1, rs.getInt("user_id"), "L'user_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("community_id"), "Il community_id inserito non corrisponde.");
        assertEquals("2025-01-29", rs.getString("subscription_date"), "La data di sottoscrizione non corrisponde.");
    }

    @Test
    void testCreatePostVotes() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='PostVotes';");
        assertTrue(rs.next(), "La tabella 'PostVotes' non è stata creata correttamente.");
    }
    @Test
    void testInsertPostVotes() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO PostVotes (user_id, post_id, vote_type) VALUES (1, 1, 1);");
        stmt.executeUpdate("INSERT INTO PostVotes (user_id, post_id, vote_type) VALUES (2, 1, 0);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato il voto per user_id = 1 e post_id = 1.");
        assertEquals(1, rs.getInt("vote_type"), "Il tipo di voto per user_id = 1 non corrisponde.");

        rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 2 AND post_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato il voto per user_id = 2 e post_id = 1.");
        assertEquals(0, rs.getInt("vote_type"), "Il tipo di voto per user_id = 2 non corrisponde.");
    }

    @Test
    void testCreatePostWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='PostWarnings';");
        assertTrue(rs.next(), "La tabella 'PostWarnings' non è stata creata correttamente.");
    }

    @Test
    void testInsertPostWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO PostWarnings (sender_id, post_id) VALUES (1, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM PostWarnings WHERE sender_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato l'avvertimento per sender_id = 1 e post_id = 1.");
        assertEquals(1, rs.getInt("sender_id"), "Il sender_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("post_id"), "Il post_id inserito non corrisponde.");
    }

    @Test
    void testCreateCommentWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CommentWarnings';");
        assertTrue(rs.next(), "La tabella 'CommentWarnings' non è stata creata correttamente.");
    }

    @Test
    void testInsertCommentWarningsData() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO CommentWarnings (sender_id, comment_id, post_id) VALUES (1, 1, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM CommentWarnings WHERE sender_id = 1 AND comment_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Non è stato trovato l'avvertimento per sender_id = 1, comment_id = 1 e post_id = 1.");
        assertEquals(1, rs.getInt("sender_id"), "Il sender_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("comment_id"), "Il comment_id inserito non corrisponde.");
        assertEquals(1, rs.getInt("post_id"), "Il post_id inserito non corrisponde.");
    }

}