package test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class SetDBTest {

    private static Connection conn;

    @BeforeAll
    static void setUp() {
        // delete database if it exists
        File dbFile = new File("database/bigDBTest.db");
        if (dbFile.exists()) {
            dbFile.delete();
            System.out.println("Database successfully deleted.");
        } else {
            System.out.println("The database does not exist.");
        }
        conn = DBConnection.open_connection("database/bigDBTest.db");
        SetDB.createDB();
    }

    @AfterAll
    static void tearDown() {
        DBConnection.disconnect();
    }

    @Test
    void testCreateUser() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='User';");
        assertTrue(rs.next(), "The User table was not created correctly.");
    }

    @Test
    void testInsertUser() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO User (nickname, name, surname) VALUES ('giorgio89', 'giorgio', 'rossi');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM User WHERE nickname = 'giorgio89';");
        assertTrue(rs.next(), "The user was not inserted correctly.");
        assertEquals("giorgio", rs.getString("name"), "The user's name does not match.");
        assertEquals("rossi", rs.getString("surname"), "The user's surname does not match.");
    }

    @Test
    void testCreateCommunity() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Community';");
        assertTrue(rs.next(), "The Community table was not created correctly.");
    }
    @Test
    void testInsertCommunity() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Community (title, description) VALUES ('Tech Community', 'A community dedicated to technology');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Community WHERE title = 'Tech Community';");
        assertTrue(rs.next(), "The community was not inserted correctly.");
        assertEquals("Tech Community", rs.getString("title"), "The community name does not match.");
        assertEquals("A community dedicated to technology", rs.getString("description"), "The community description does not match.");
    }

    @Test
    void testCreateUserAccess() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='UserAccess';");
        assertTrue(rs.next(), "The UserAccess table was not created correctly.");
    }

    @Test
    void testInsertUserAccess() throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("INSERT INTO UserAccess (email, user_id, password, authen) VALUES ('test@gmail.com', 1, 'Password123', 1);");
        stmt.executeUpdate("INSERT INTO UserAccess (email, user_id, password, authen) VALUES ('test2@gmail.com', 2, 'Password345', 0);");

        ResultSet rs1 = stmt.executeQuery("SELECT * FROM UserAccess WHERE email = 'test@gmail.com';");
        assertTrue(rs1.next(), "The first user's data was not inserted correctly.");
        assertEquals("test@gmail.com", rs1.getString("email"), "The first user's email does not match.");
        assertEquals(1, rs1.getInt("user_id"), "The first user's user_id does not match.");
        assertEquals("Password123", rs1.getString("password"), "The first user's password does not match.");
        assertEquals(1, rs1.getInt("authen"), "The first user's 'authen' value does not match.");

        ResultSet rs2 = stmt.executeQuery("SELECT * FROM UserAccess WHERE email = 'test2@gmail.com';");
        assertTrue(rs2.next(), "The second user's data was not inserted correctly.");
        assertEquals("test2@gmail.com", rs2.getString("email"), "The second user's email does not match.");
        assertEquals(2, rs2.getInt("user_id"), "The second user's user_id does not match.");
        assertEquals("Password345", rs2.getString("password"), "The second user's password does not match.");
        assertEquals(0, rs2.getInt("authen"), "The second user's 'authen' value does not match.");
    }

    @Test
    void testCreateRules() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Rules';");
        assertTrue(rs.next(), "The Rules table was not created correctly.");
    }

    @Test
    void testInsertRules() throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("INSERT INTO Rules (id, community_id, content) VALUES (1, 1, 'Rule 1');");
        stmt.executeUpdate("INSERT INTO Rules (id, community_id, content) VALUES (2, 1, 'Rule 2');");

        ResultSet rs1 = stmt.executeQuery("SELECT * FROM Rules WHERE content = 'Rule 1';");
        assertTrue(rs1.next(), "The rule 'Rule 1' was not inserted correctly.");
        assertEquals("Rule 1", rs1.getString("content"), "The rule content does not match.");
        assertEquals(1, rs1.getInt("community_id"), "The community ID does not match.");

        ResultSet rs2 = stmt.executeQuery("SELECT * FROM Rules WHERE content = 'Rule 2';");
        assertTrue(rs2.next(), "The rule 'Rule 2' was not inserted correctly.");
        assertEquals("Rule 2", rs2.getString("content"), "The rule content does not match.");
        assertEquals(1, rs2.getInt("community_id"), "The community ID does not match.");
    }

    @Test
    void testRulesTrigger() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM Rules;");

        // Insert two rules into the 'Rules' table
        stmt.executeUpdate("INSERT INTO Rules (community_id, content) VALUES (2, 'Rule 1 test');");
        stmt.executeUpdate("INSERT INTO Rules (community_id, content) VALUES (2, 'Rule 2 test');");

        // Verify that the ID of the second rule is 2 (auto-increment)
        ResultSet rs = stmt.executeQuery("SELECT id, content FROM Rules WHERE content = 'Rule 2 test';");
        assertTrue(rs.next(), "The rule 'Rule 2' was not inserted correctly.");
        assertEquals(2, rs.getInt("id"), "The ID of the second rule was not incremented correctly.");
        assertEquals("Rule 2 test", rs.getString("content"), "The rule content does not match.");
    }
    @Test
    void testCreatePost() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Post';");
        assertTrue(rs.next(), "The Post table was not created correctly.");
    }

    @Test
    void testInsertPost() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Post (time, content, user_id, community_id) VALUES ('2025-01-29 12:00:00', 'Post content 1', 1, 1);");
        stmt.executeUpdate("INSERT INTO Post (time, content, user_id, community_id) VALUES ('2025-01-29 14:00:00', 'Post content 2', 2, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Post WHERE user_id = 1;");
        assertTrue(rs.next(), "Post for user_id = 1 not found.");
        assertEquals("2025-01-29 12:00:00", rs.getString("time"), "The time inserted does not match.");
        assertEquals("Post content 1", rs.getString("content"), "The post content does not match.");
        assertEquals(1, rs.getInt("user_id"), "The user_id inserted does not match.");
        assertEquals(1, rs.getInt("community_id"), "The community_id inserted does not match.");

        rs = stmt.executeQuery("SELECT * FROM Post WHERE user_id = 2;");
        assertTrue(rs.next(), "Post for user_id = 2 not found.");
        assertEquals("2025-01-29 14:00:00", rs.getString("time"), "The time inserted does not match.");
        assertEquals("Post content 2", rs.getString("content"), "The post content does not match.");
        assertEquals(2, rs.getInt("user_id"), "The user_id inserted does not match.");
        assertEquals(1, rs.getInt("community_id"), "The community_id inserted does not match.");
    }

    @Test
    void testCreateComment() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Comment';");
        assertTrue(rs.next(), "The Comment table was not created correctly.");
    }

    @Test
    void testInsertComment() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Comment (post_id, level, user_id, content) VALUES (1, 0, 1, 'Comment content 1');");
        stmt.executeUpdate("INSERT INTO Comment (post_id, level, user_id, content) VALUES (1, 0, 2, 'Comment content 2');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Comment WHERE user_id = 1;");
        assertTrue(rs.next(), "Comment for user_id = 1 not found.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(0, rs.getInt("level"), "The level inserted does not match.");
        assertEquals("Comment content 1", rs.getString("content"), "The comment content does not match.");

        rs = stmt.executeQuery("SELECT * FROM Comment WHERE user_id = 2;");
        assertTrue(rs.next(), "Comment for user_id = 2 not found.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(0, rs.getInt("level"), "The level inserted does not match.");
        assertEquals("Comment content 2", rs.getString("content"), "The comment content does not match.");
    }

    @Test
    void testCommentTrigger() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM Comment;");
        stmt.executeUpdate("INSERT INTO Post (time, content, user_id, community_id) VALUES ('2023-10-01', 'Test post', 1, 1);");
        stmt.executeUpdate("INSERT INTO Comment (post_id, user_id, content) VALUES (1, 1, 'Comment 1');");
        stmt.executeUpdate("INSERT INTO Comment (post_id, user_id, content) VALUES (1, 1, 'Comment 2');");

        ResultSet rs = stmt.executeQuery("SELECT id FROM Comment WHERE content = 'Comment 2';");
        assertTrue(rs.next(), "The auto-increment trigger is not working correctly.");
        assertEquals(2, rs.getInt("id"), "The comment ID was not incremented correctly.");
    }

//todo testCommentWithoutPost to finish...
//    @Test
//    void testCommentWithoutPost() throws SQLException {
//        Statement stmt = conn.createStatement();
//        stmt.executeUpdate("DELETE FROM Comment;");
//        stmt.executeUpdate("PRAGMA foreign_keys = ON;");
//        stmt.executeUpdate("INSERT INTO Comment (post_id, user_id, content) VALUES (100, 1, 'Comment without post');");
//
//        ResultSet rs = stmt.executeQuery("SELECT * FROM Comment WHERE post_id = 100;");
//        assertEquals(0, rs.getInt("id"), "The comment should not have been inserted.");
//    }

    @Test
    void testCreateCommentHierarchy() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CommentHierarchy';");
        assertTrue(rs.next(), "The 'CommentHierarchy' table was not created correctly.");
    }

    @Test
    void testInsertCommentHierarchy() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO CommentHierarchy (post_id, parent_id, child_id) VALUES (1, 1, 2);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM CommentHierarchy WHERE post_id = 1 AND parent_id = 1;");
        assertTrue(rs.next(), "Hierarchy for post_id = 1 not found.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(1, rs.getInt("parent_id"), "The parent_id inserted does not match.");
        assertEquals(2, rs.getInt("child_id"), "The child_id inserted does not match.");
    }

    @Test
    void testCreateBannedUsers() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='BannedUsers';");
        assertTrue(rs.next(), "The 'BannedUsers' table was not created correctly.");
    }

    @Test
    void testInsertBannedUsers() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO BannedUsers (user_id, community_id, ban_date, reason) VALUES (1, 1, '2025-01-29', 'Ban reason');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM BannedUsers WHERE user_id = 1;");
        assertTrue(rs.next(), "Banned user with user_id = 1 not found.");
        assertEquals(1, rs.getInt("user_id"), "The user_id inserted does not match.");
        assertEquals(1, rs.getInt("community_id"), "The community_id inserted does not match.");
        assertEquals("2025-01-29", rs.getString("ban_date"), "The ban date inserted does not match.");
        assertEquals("Ban reason", rs.getString("reason"), "The ban reason does not match.");
    }

    @Test
    void testCreateModerator() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Moderator';");
        assertTrue(rs.next(), "The 'Moderator' table was not created correctly.");
    }

    @Test
    void testInsertModerator() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Moderator (user_id, community_id, assigned_date) VALUES (1, 1, '2025-01-29');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Moderator WHERE user_id = 1;");
        assertTrue(rs.next(), "Moderator with user_id = 1 not found.");
        assertEquals(1, rs.getInt("user_id"), "The user_id inserted does not match.");
        assertEquals(1, rs.getInt("community_id"), "The community_id inserted does not match.");
        assertEquals("2025-01-29", rs.getString("assigned_date"), "The assigned date does not match.");
    }

    @Test
    void testCreateSubscription() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Subscription';");
        assertTrue(rs.next(), "The 'Subscription' table was not created correctly.");
    }

    @Test
    void testInsertSubscription() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Subscription (user_id, community_id, subscription_date) VALUES (1, 1, '2025-01-29');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Subscription WHERE user_id = 1;");
        assertTrue(rs.next(), "Subscription with user_id = 1 not found.");
        assertEquals(1, rs.getInt("user_id"), "The user_id inserted does not match.");
        assertEquals(1, rs.getInt("community_id"), "The community_id inserted does not match.");
        assertEquals("2025-01-29", rs.getString("subscription_date"), "The subscription date does not match.");
    }

    @Test
    void testCreatePostVotes() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='PostVotes';");
        assertTrue(rs.next(), "The 'PostVotes' table was not created correctly.");
    }

    @Test
    void testInsertPostVotes() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO PostVotes (user_id, post_id, vote_type) VALUES (1, 1, 1);");
        stmt.executeUpdate("INSERT INTO PostVotes (user_id, post_id, vote_type) VALUES (2, 1, 0);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Vote for user_id = 1 and post_id = 1 not found.");
        assertEquals(1, rs.getInt("vote_type"), "The vote type for user_id = 1 does not match.");

        rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 2 AND post_id = 1;");
        assertTrue(rs.next(), "Vote for user_id = 2 and post_id = 1 not found.");
        assertEquals(0, rs.getInt("vote_type"), "The vote type for user_id = 2 does not match.");
    }

    @Test
    void testCreatePostWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='PostWarnings';");
        assertTrue(rs.next(), "The 'PostWarnings' table was not created correctly.");
    }

    @Test
    void testInsertPostWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO PostWarnings (sender_id, post_id) VALUES (1, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM PostWarnings WHERE sender_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Warning for sender_id = 1 and post_id = 1 not found.");
        assertEquals(1, rs.getInt("sender_id"), "The sender_id inserted does not match.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
    }

    @Test
    void testCreateCommentWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CommentWarnings';");
        assertTrue(rs.next(), "The 'CommentWarnings' table was not created correctly.");
    }

    @Test
    void testInsertCommentWarningsData() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO CommentWarnings (sender_id, comment_id, post_id) VALUES (1, 1, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM CommentWarnings WHERE sender_id = 1 AND comment_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Warning for sender_id = 1, comment_id = 1, and post_id = 1 not found.");
        assertEquals(1, rs.getInt("sender_id"), "The sender_id inserted does not match.");
        assertEquals(1, rs.getInt("comment_id"), "The comment_id inserted does not match.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
    }
}