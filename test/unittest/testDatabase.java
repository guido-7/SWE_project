package test.unittest;

import org.junit.jupiter.api.*;
import src.services.CommunityService;
import src.persistence.dbmanager.DBConnection;
import src.persistence.dbmanager.SetDB;
import src.persistence.DAOs.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SetDBTest {
    private static Connection conn;
    static String url = "database/bigDBTest.db";
    private static final CommentDAO commentDAO = new CommentDAO();
    private static final CommunityDAO communityDAO = new CommunityDAO();
    private static final PostDAO postDAO = new PostDAO();
    private static final RulesDAO rulesDAO = new RulesDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final ModeratorDAO moderatorDAO = new ModeratorDAO();

    @BeforeAll
    static void setUp() {
        DBConnection.changeDBPath(url);
        File dbFile = new File(url);
        if (dbFile.exists()) {
            boolean isDeleted = dbFile.delete();
            if(isDeleted)
                System.out.println("Database deleted successfully");
            else
                System.out.println("Database not deleted successfully");
        } else {
            System.out.println("The database does not exist.");
        }
        conn = DBConnection.open_connection();
        SetDB.createDB();
    }

    @AfterAll
    static void tearDown() {
        DBConnection.disconnect();
    }

    @BeforeEach
    void clearAllTable() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DBConnection.open_connection();
        }
        conn.setAutoCommit(true);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = OFF;");

            List<String> tables = Arrays.asList(
                    "Admin", "BannedUsers", "Comment", "CommentHierarchy",
                    "CommentVotes", "CommentWarnings", "Community",
                    "Moderator", "Post", "PostVotes", "PostWarnings",
                    "Rules", "SavedPost", "Subscription", "TimeOut",
                    "User", "UserAccess", "UserDescription"
            );
            for (String t : tables) {
                stmt.executeUpdate("DELETE FROM " + t + ";");
            }

            // Reset the sequence for all tables with AUTOINCREMENT
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name = 'User';");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name = 'Community';");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name = 'Post';");

            stmt.executeUpdate("PRAGMA foreign_keys = ON;");
        }
        conn.close();

        try (Connection connVacuum = DBConnection.open_connection(url);
                Statement vacuumStmt = connVacuum.createStatement()) {
            vacuumStmt.executeUpdate("VACUUM;");
        }

        conn = DBConnection.open_connection();
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

        rs = stmt.executeQuery("SELECT * FROM User WHERE id = 1;");
        assertEquals("giorgio89", rs.getString("nickname"), "The user's nickname does not match.");
        assertEquals("giorgio", rs.getString("name"), "The user's name does not match.");
        assertEquals("rossi", rs.getString("surname"), "The user's surname does not match.");

        userDAO.save(Map.of("nickname", "mario1", "name", "mario", "surname", "rossi"));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM User WHERE id = 2;");
        assertEquals("mario1", rs.getString("nickname"), "The user's nickname does not match.");
        assertEquals("mario", rs.getString("name"), "The user's name does not match.");
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

        rs = stmt.executeQuery("SELECT * FROM Community WHERE id = 1;");
        assertEquals("Tech Community", rs.getString("title"), "The community name does not match.");
        assertEquals("A community dedicated to technology", rs.getString("description"), "The community description does not match.");

        communityDAO.save(Map.of("title", "News", "description", "A community dedicated to news"));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM Community WHERE id = 2;");
        assertEquals("News", rs.getString("title"), "The community name does not match.");
        assertEquals("A community dedicated to news", rs.getString("description"), "The community description does not match.");
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
        stmt.executeUpdate("INSERT INTO User (nickname, name, surname) VALUES ('Luigi1', 'luigi', 'bianchi');");
        stmt.executeUpdate("INSERT INTO User (nickname, name, surname) VALUES ('Mario1', 'mario', 'rossi');");
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
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();

        stmt.executeUpdate("INSERT INTO Rules (id, community_id, title, content, priority) VALUES (1, 1, 'Rule 1', 'No insulti', 3);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Rules WHERE title = 'Rule 1';");
        assertTrue(rs.next(), "The rule 'Rule 1' was not inserted correctly.");
        assertEquals("Rule 1", rs.getString("title"), "The rule title does not match.");
        assertEquals("No insulti", rs.getString("content"), "The rule content does not match.");
        assertEquals(3, rs.getInt("priority"), "The rule priority does not match.");
        assertEquals(1, rs.getInt("community_id"), "The community ID does not match.");
        rulesDAO.save(Map.of("community_id", 1, "title", "Rule 2", "content", "No spam", "priority", 2));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM Rules WHERE title = 'Rule 2';");
        assertTrue(rs.next(), "The rule 'Rule 2' was not inserted correctly.");
        assertEquals("Rule 2", rs.getString("title"), "The rule title does not match.");
        assertEquals("No spam", rs.getString("content"), "The rule content does not match.");
        assertEquals(2, rs.getInt("priority"), "The rule priority does not match.");
    }

    @Test
    void testRulesTrigger() throws SQLException {
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("INSERT INTO Rules (community_id, title, content) VALUES (1, 'title 1', 'Rule 1 test');");
        stmt.executeUpdate("INSERT INTO Rules (community_id, title, content) VALUES (1, 'title 2', 'Rule 2 test');");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Rules WHERE title = 'title 1';");
        assertTrue(rs.next(), "The rule 'Rule 1' was not inserted correctly.");
        assertEquals(1, rs.getInt("id"), "The ID of the first rule was not incremented correctly.");
        assertEquals("Rule 1 test", rs.getString("content"), "The rule content does not match.");

        // Verify that the ID of the second rule is 2 (auto-increment)
        rs = stmt.executeQuery("SELECT * FROM Rules WHERE title = 'title 2';");
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
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        userDAO.save(Map.of("nickname", "Mario1", "name", "mario", "surname", "rossi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Post (time, title, content, user_id, community_id) VALUES ('2025-01-29 12:00:00', 'Title 1', 'Post content 1', 1, 1);");
        stmt.executeUpdate("INSERT INTO Post (time, title, content, user_id, community_id) VALUES ('2025-01-29 14:00:00', 'Title 2', 'Post content 2', 2, 1);");

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
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO Comment (post_id, level, user_id, content, community_id) VALUES (1, 0, 1, 'Comment content 1', 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM Comment WHERE user_id = 1;");
        assertTrue(rs.next(), "Comment for user_id = 1 not found.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(0, rs.getInt("level"), "The level inserted does not match.");
        assertEquals("Comment content 1", rs.getString("content"), "The comment content does not match.");

        postDAO.save(Map.of( "title", "Title 2", "content", "Post content 2", "user_id", 1, "community_id", 1));
        //conn = DBConnection.open_connection(url);
        commentDAO.save(Map.of("post_id", 2, "level", 0, "user_id", 1, "content", "Comment content 2", "community_id", 1));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM Comment WHERE post_id = 2 AND user_id = 1;");
        assertTrue(rs.next(), "Comment for user_id = 1 not found.");
        assertEquals(2, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(0, rs.getInt("level"), "The level inserted does not match.");
        assertEquals("Comment content 2", rs.getString("content"), "The comment content does not match.");
    }

    @Test
    void testCommentTrigger() throws SQLException {
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        userDAO.save(Map.of("nickname", "Mario1", "name", "mario", "surname", "rossi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));
        //conn = DBConnection.open_connection(url);
        commentDAO.save(Map.of("post_id", 1, "level", 0, "user_id", 1, "content", "Comment 1", "community_id", 1));
        //conn = DBConnection.open_connection();
        commentDAO.save(Map.of("post_id", 1, "level", 0, "user_id", 2, "content", "Comment 2", "community_id", 1));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT id FROM Comment WHERE content = 'Comment 2';");
        assertTrue(rs.next(), "The auto-increment trigger is not working correctly.");
        assertEquals(2, rs.getInt("id"), "The comment ID was not incremented correctly.");
    }

    @Test
    void testCreateCommentHierarchy() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CommentHierarchy';");
        assertTrue(rs.next(), "The 'CommentHierarchy' table was not created correctly.");
    }

    @Test
    void testInsertCommentHierarchy() throws SQLException {
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));
        //conn = DBConnection.open_connection(url);
        commentDAO.save(Map.of("post_id", 1, "level", 0, "user_id", 1, "content", "Comment content 1", "community_id", 1));
        //conn = DBConnection.open_connection(url);
        commentDAO.save(Map.of("post_id", 1, "level", 1, "user_id", 1, "content", "Comment content 2", "community_id", 1));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO CommentHierarchy (post_id, parent_id, child_id) VALUES (1, 1, 2);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM CommentHierarchy WHERE post_id = 1 AND parent_id = 1;");
        assertTrue(rs.next(), "Hierarchy for post_id = 1 not found.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(1, rs.getInt("parent_id"), "The parent_id inserted does not match.");
        assertEquals(2, rs.getInt("child_id"), "The child_id inserted does not match.");

        commentDAO.save(Map.of("post_id", 1, "level", 2, "user_id", 1, "content", "Comment contenut 3", "community_id", 1));
        conn = DBConnection.open_connection(url);
        commentDAO.saveCommentRelation(Map.of("post_id", 1, "parent_id", 2, "child_id", 3));

        conn = DBConnection.open_connection(url);
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM CommentHierarchy WHERE post_id = 1 AND parent_id = 2;");
        assertTrue(rs.next(), "Hierarchy for post_id = 1 not found.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
        assertEquals(2, rs.getInt("parent_id"), "The parent_id inserted does not match.");
        assertEquals(3, rs.getInt("child_id"), "The child_id inserted does not match.");
    }

    @Test
    void testCreateBannedUsers() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='BannedUsers';");
        assertTrue(rs.next(), "The 'BannedUsers' table was not created correctly.");
    }

    @Test
    void testInsertBannedUsers() throws SQLException {
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));

        conn = DBConnection.open_connection();
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
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));

        conn = DBConnection.open_connection();
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
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));

        conn = DBConnection.open_connection();
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
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        userDAO.save(Map.of("nickname", "Mario1", "name", "mario", "surname", "rossi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO PostVotes (user_id, post_id, community_id, vote_type) VALUES (1, 1, 1, 0);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Vote for user_id = 1 and post_id = 1 not found.");
        assertEquals(0, rs.getInt("vote_type"), "The vote type for user_id = 1 does not match.");
        assertEquals(1, rs.getInt("post_id"), "The post_id for user_id = 1 does not match.");

        stmt.executeUpdate("INSERT INTO PostVotes (user_id, post_id, community_id, vote_type) VALUES (2, 1, 1, 1);");

        rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 2 AND post_id = 1;");
        assertTrue(rs.next(), "Vote for user_id = 2 and post_id = 1 not found.");
        assertEquals(1, rs.getInt("vote_type"), "The vote type for user_id = 2 does not match.");
        assertEquals(1, rs.getInt("post_id"), "The post_id for user_id = 2 does not match.");

        userDAO.save(Map.of("nickname", "Luigi2", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        userDAO.insertPostVotes(Map.of("user_id", 3, "post_id", 1, "community_id", 1, "vote_type", 1));
        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 3 AND post_id = 1;");

        assertTrue(rs.next(), "Vote for user_id = 2 and post_id = 1 not found.");
        assertEquals(1, rs.getInt("vote_type"), "The vote type for user_id = 2 does not match.");
        assertEquals(1, rs.getInt("post_id"), "The post_id for user_id = 2 does not match.");

        userDAO.insertPostVotes(Map.of("user_id", 3, "post_id", 1, "community_id", 1, "vote_type", 0));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM PostVotes WHERE user_id = 3 AND post_id = 1;");
        assertTrue(rs.next(), "Vote for user_id = 3 and post_id = 1 not found.");
        assertEquals(0, rs.getInt("vote_type"), "The vote type for user_id = 3 does not match.");
    }

    @Test
    void testTriggerUpdatePostLikes() throws SQLException {
        // Verify that likes in Post table are updated correctly
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        userDAO.save(Map.of("nickname", "Mario1", "name", "mario", "surname", "rossi"));
        //conn = DBConnection.open_connection(url);
        userDAO.save(Map.of("nickname", "Luigi2", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));
        //conn = DBConnection.open_connection(url);
        userDAO.insertPostVotes(Map.of("user_id", 1, "post_id", 1, "community_id", 1, "vote_type", 1));
        //conn = DBConnection.open_connection(url);
        userDAO.insertPostVotes(Map.of("user_id", 2, "post_id", 1, "community_id", 1, "vote_type", 1));
        //conn = DBConnection.open_connection(url);
        userDAO.insertPostVotes(Map.of("user_id", 3, "post_id", 1, "community_id", 1, "vote_type", 0));

        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Post WHERE id = 1;");
        assertTrue(rs.next(), "Post with id = 1 not found.");
        assertEquals(2, rs.getInt("likes"), "The likes in Post does not match.");
        assertEquals(1, rs.getInt("dislikes"), "The dislikes in Post does not match.");

        // Test replace
        userDAO.insertPostVotes(Map.of("user_id", 3, "post_id", 1, "community_id", 1, "vote_type", 1));

        conn = DBConnection.open_connection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM Post WHERE id = 1;");
        assertTrue(rs.next(), "Post with id = 1 not found.");
        assertEquals(3, rs.getInt("likes"), "The likes in Post does not match.");
        assertEquals(0, rs.getInt("dislikes"), "The dislikes in Post does not match.");
    }

    @Test
    void testCreatePostWarnings() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='PostWarnings';");
        assertTrue(rs.next(), "The 'PostWarnings' table was not created correctly.");
    }

    @Test
    void testInsertPostWarnings() throws SQLException {
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));
        //conn = DBConnection.open_connection(url);
        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO PostWarnings (sender_id, post_id, community_id) VALUES (1, 1, 1);");

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
        userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        //conn = DBConnection.open_connection(url);
        communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        //conn = DBConnection.open_connection(url);
        postDAO.save(Map.of( "title", "Title 1", "content", "Post content 1", "user_id", 1, "community_id", 1));
        //conn = DBConnection.open_connection(url);
        commentDAO.save(Map.of("post_id", 1, "level", 0, "user_id", 1, "content", "Comment content 1", "community_id", 1));
        //conn = DBConnection.open_connection(url);
        conn = DBConnection.open_connection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO CommentWarnings (sender_id, comment_id, post_id, community_id) VALUES (1, 1, 1, 1);");

        ResultSet rs = stmt.executeQuery("SELECT * FROM CommentWarnings WHERE sender_id = 1 AND comment_id = 1 AND post_id = 1;");
        assertTrue(rs.next(), "Warning for sender_id = 1, comment_id = 1, and post_id = 1 not found.");
        assertEquals(1, rs.getInt("sender_id"), "The sender_id inserted does not match.");
        assertEquals(1, rs.getInt("comment_id"), "The comment_id inserted does not match.");
        assertEquals(1, rs.getInt("post_id"), "The post_id inserted does not match.");
    }
    @Test
    void testBanUser() throws SQLException {
        //create community and user
        int reportedId = userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        int communityId = communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        String banReason = "Ban reason";
        communityDAO.banUser(reportedId,communityId,banReason);
        assertTrue(communityDAO.isBanned(reportedId, communityId));

    }
    @Test
    void testTimeOutUser() throws SQLException {
        int reportedId = userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        int communityId = communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        communityDAO.timeOutUser(reportedId,communityId, LocalDateTime.now());
        assertTrue(communityDAO.isTimedOut(reportedId, communityId));

    }
    @Test
    void testPromoteUser() throws SQLException {
        int reportedId = userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        int communityId = communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        CommunityService communityService = new CommunityService(communityId);
        communityService.promoteToModerator(reportedId);
        assertTrue(communityService.isModerator(reportedId));

    }
    @Test
    void testDismissUser() throws SQLException {
        int reportedId = userDAO.save(Map.of("nickname", "Luigi1", "name", "luigi", "surname", "bianchi"));
        int communityId = communityDAO.save(Map.of("title", "Tech Community", "description", "A community dedicated to technology"));
        moderatorDAO.save(Map.of("user_id", reportedId, "community_id", communityId, "assigned_date", LocalDateTime.now()));
        CommunityService communityService = new CommunityService(communityId);
        assertTrue(communityService.isModerator(reportedId));
        communityService.downgradeModerator(reportedId);
        assertFalse(communityService.isModerator(reportedId));

    }
}