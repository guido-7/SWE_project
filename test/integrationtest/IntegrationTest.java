package test.integrationtest;

import javafx.scene.control.Label;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.businesslogic.*;
import src.domainmodel.User;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.*;
import src.servicemanager.GuestContext;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    // Connection
    private static Connection conn;
    static String url = "database/bigDBTest.db";

    // Parameters
    int USER_ID = 1;
    int COMMUNITY_ID = 1;
    final int LIKE = 1;
    final int DISLIKE = 0;
    int POST_ID = 1;
    int COMMENT_ID = 1;
    int LEVEL_0 = 0;
    String USER_NAME = "Luigi";
    String USER_SURNAME = "Bianchi";
    String USER_NICKNAME = "user_test";
    String USER_PASSWORD = "12345678";
    String COMMUNITY_TITLE = "Test Community";
    String COMMUNITY_DESCRIPTION = "Test Description";
    String POST_TITLE = "Test Title";
    String POST_CONTENT = "Test Content";
    String COMMENT_CONTENT = "Test Comment Content";

    // DAO
    PostDAO postDAO = new PostDAO();
    CommentDAO commentDAO = new CommentDAO();
    CommunityDAO communityDAO = new CommunityDAO();
    UserDAO userDAO = new UserDAO();

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

        try (Connection connVacuum = DBConnection.open_connection();
             Statement vacuumStmt = connVacuum.createStatement()) {
            vacuumStmt.executeUpdate("VACUUM;");
        }

        conn = DBConnection.open_connection();
    }

    // Test to add and remove like from a post
    @Test
    void AddRemovePostLike() throws SQLException {
        USER_ID = createUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);
        POST_ID = createPost(POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);

        // Insert the like to the Post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleLike(user);

        assertEquals(LIKE, userDAO.getPostVote(USER_ID, POST_ID), "Post like not inserted correctly");

        // Remove like from the Post
        postService.toggleLike(user);

        assertNull(userDAO.getPostVote(USER_ID, POST_ID), "Post like not removed correctly");
    }

    // Test to add and remove dislike from a post
    @Test
    void AddRemovePostDislike() throws SQLException {
        USER_ID = createUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);
        POST_ID = createPost(POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);

        // Insert the dislike to the Post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleDislike(user);

        assertEquals(DISLIKE, userDAO.getPostVote(USER_ID, POST_ID), "Post dislike not inserted correctly");

        // Remove dislike from the Post
        postService.toggleDislike(user);

        assertNull(userDAO.getPostVote(USER_ID, POST_ID), "Post dislike not removed correctly");
    }

    // Test to check the user's vote on a post
    @Test
    void checkUserVote() throws SQLException {
        USER_ID = createUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);
        POST_ID = createPost(POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);

        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleLike(user);

        assertTrue(postService.isLiked(USER_ID));
        assertFalse(postService.isDisliked(USER_ID));

        postService.toggleDislike(user);
        assertFalse(postService.isLiked(USER_ID));
        assertTrue(postService.isDisliked(USER_ID));
    }

    // Test to add and remove like from a comment
    @Test
    void AddRemoveCommentLike() throws SQLException {
        USER_ID = createUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);
        POST_ID = createPost(POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);
        COMMENT_ID = createComment(POST_ID, LEVEL_0, USER_ID, COMMENT_CONTENT, COMMUNITY_ID);

        User user = userDAO.findById(USER_ID).orElse(null);
        CommentService commentService = new CommentService(commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null));
        commentService.toggleLike(user);

        assertEquals(LIKE, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID), "Comment like not inserted correctly");

        commentService.toggleLike(user);
        assertNull(userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID), "Comment like not removed correctly");
    }

    // Test to add and remove dislike from a comment
    @Test
    void addRemoveCommentDislike() throws SQLException {
        USER_ID = createUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);
        POST_ID = createPost(POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);
        COMMENT_ID = createComment(POST_ID, LEVEL_0, USER_ID, COMMENT_CONTENT, COMMUNITY_ID);

        User user = userDAO.findById(USER_ID).orElse(null);
        CommentService commentService = new CommentService(commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null));
        commentService.toggleDislike(user);

        assertEquals(DISLIKE, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID), "Comment dislike not inserted correctly");

        commentService.toggleDislike(user);
        assertNull(userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID), "Comment dislike not removed correctly");
    }

    // Test for checking user's vote on a comment
    @Test
    void checkUserVoteComment() throws SQLException {
        USER_ID = createUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);
        POST_ID = createPost(POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);
        COMMENT_ID = createComment(POST_ID, LEVEL_0, USER_ID, COMMENT_CONTENT, COMMUNITY_ID);

        User user = userDAO.findById(USER_ID).orElse(null);
        CommentService commentService = new CommentService(commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null));
        commentService.toggleLike(user);

        assertTrue(commentService.isLiked(USER_ID));
        assertFalse(commentService.isDisliked(USER_ID));

        commentService.toggleDislike(user);
        assertFalse(commentService.isLiked(USER_ID));
        assertTrue(commentService.isDisliked(USER_ID));
    }

    // Test for user registration
    @Test
    void registerUserTest() throws SQLException {
        // Creo User ma senza registrazione
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));

        // Attempt login with an unregistered user
        assertFalse(userDAO.isValidUser(USER_NICKNAME, USER_PASSWORD));

        // Register the user
        userDAO.registerUserAccessInfo(USER_ID, USER_NICKNAME, USER_PASSWORD);

        // Check if the user is successfully registered
        assertTrue(userDAO.isRegisteredUser(USER_NICKNAME));
    }

    // Test for subscribing to a community
    @Test
    void subscribeCommunityTest() throws SQLException {
        // Register a user
        registerUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);

        // Create a community and initialize the service
        CommunityService communityService = new CommunityService(COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));

        // Verify that the user is not subscribed
        assertFalse(communityService.isSubscribed());

        // Subscribe the user to the community
        assertTrue(communityService.subscribe());
        assertTrue(communityService.isSubscribed());

        // Unsubscribe the user from the community
        assertTrue(communityService.unsubscribe());
        assertFalse(communityService.isSubscribed());
    }

    // Test for checking if a user is banned
    @Test
    void checkBannedUserTest() throws SQLException {
        // Register a user
        USER_ID = registerUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);

        // Create a community and initialize the service
        CommunityService communityService = new CommunityService(createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));

        // Verify that the user is not banned
        assertFalse(communityService.checkBannedUser());

        // Ban the user from the community
        communityService.banUser(USER_ID, "Violation of rules");
        assertTrue(communityService.checkBannedUser());
    }

    // Test for checking if a user is a moderator
    @Test
    void checkModeratorTest() throws SQLException {
        // Register a user
        USER_ID = registerUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);

        // Create a community and initialize the service
        CommunityService communityService = new CommunityService(createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));

        // Verify that the user is not a moderator
        assertFalse(communityService.isModerator(USER_ID));

        // Promote the user to moderator
        communityService.promoteToModerator(USER_ID);
        assertTrue(communityService.isModerator(USER_ID));

        // Downgrade the user from moderator
        communityService.downgradeModerator(USER_ID);
        assertFalse(communityService.isModerator(USER_ID));
    }

    // Test for checking if a user is an admin
    @Test
    void checkAdminTest() throws SQLException {
        // Register a user
        USER_ID = registerUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);

        // Create a community and initialize the service
        CommunityService communityService = new CommunityService(createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));

        // Verify that the user is not an admin
        assertNull(communityService.getAdmin(USER_ID));

        // Promote the user to admin
        communityService.promoteToAdmin(USER_ID);
        assertTrue(communityService.isAdmin(USER_ID));

        // Downgrade the user from admin
        communityService.downgradeAdmin(USER_ID);
        assertFalse(communityService.isAdmin(USER_ID));
    }

    // Test for creating and deleting a community
    @Test
    void createDeleteCommunityTest() throws SQLException {
        // Register a user
        registerUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);

        // Create the community
        CommunityCreationService communityCreationService = new CommunityCreationService();

        // Verify the community is created
        assertEquals(COMMUNITY_ID, communityCreationService.createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));
        assertNotNull(communityDAO.findById(COMMUNITY_ID).orElse(null));

        // Delete the community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        communityService.deleteCommunity();

        // Verify the community is deleted
        assertNull(communityDAO.findById(COMMUNITY_ID).orElse(null));
    }

    // Test for creating and deleting a post
    @Test
    void createDeletePostTest() throws SQLException {
        // Register a user
        USER_ID = registerUser(USER_NICKNAME, USER_NAME, USER_SURNAME, USER_PASSWORD);

        // Create a community
        COMMUNITY_ID = createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION);

        // Create a post
        PostCreationService postCreationService = new PostCreationService();
        postCreationService.createPost(COMMUNITY_TITLE, POST_TITLE, POST_CONTENT, COMMUNITY_ID, USER_ID);

        // Verify the post is created
        assertNotNull(postDAO.findById(POST_ID).orElse(null));
        assertEquals(POST_ID, postDAO.findById(POST_ID).orElse(null).getId());

        // Delete the post
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.deletePost(POST_ID);

        // Verify the post is deleted
        assertNull(postDAO.findById(POST_ID).orElse(null));
    }

    private int createUser(String nickname, String name, String surname, String password) throws SQLException {
        int id = userDAO.save(Map.of("nickname", nickname, "name", name, "surname", surname));
        userDAO.registerUserAccessInfo(id, nickname, password);
        return id;
    }

    private int createCommunity(String title, String description) throws SQLException {
        return communityDAO.save(Map.of("title", title, "description", description));
    }

    private int createPost(String title, String content, int communityId, int userId) throws SQLException {
        return postDAO.save(Map.of("title", title, "content", content, "community_id", communityId, "user_id", userId));
    }

    private int createComment(int postId, int level, int userId, String content, int communityId) throws SQLException {
        return commentDAO.save(Map.of("post_id", postId, "level", level, "user_id", userId, "content", content, "community_id", communityId));
    }

    private int registerUser(String nickname, String name, String surname, String password) throws SQLException {
        int id = userDAO.save(Map.of("nickname", nickname, "name", name, "surname", surname));
        userDAO.registerUserAccessInfo(id, nickname, password);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));
        return id;
    }

}
