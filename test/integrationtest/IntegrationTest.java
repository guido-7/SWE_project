package test.integrationtest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.businesslogic.CommentService;
import src.businesslogic.CommunityService;
import src.businesslogic.PostService;
import src.domainmodel.Comment;
import src.domainmodel.Guest;
import src.domainmodel.User;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommentDAO;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;
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

    // Connessione
    private static Connection conn;
    static String url = "database/bigDBTest.db";

    // Parametri
    final int USER_ID = 1;
    final int COMMUNITY_ID = 1;
    final int LIKE = 1;
    final int DISLIKE = 0;
    int POST_ID = 1;
    int COMMENT_ID = 1;
    String USER_NAME = "Luigi";
    String USER_SURNAME = "Bianchi";
    String USER_NICKNAME = "user_test";
    String USER_PASSWORD = "12345678";
    String COMMUNITY_TITLE = "Test Community";
    String COMMUNITY_DESCRIPTION = "Test Description";
    String POST_TITLE = "Test Title";
    String POST_CONTENT = "Test Content";


    // DAO
    PostDAO postDAO = new PostDAO();
    CommentDAO commentDAO = new CommentDAO();
    CommunityDAO communityDAO = new CommunityDAO();
    UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void setUp() {
        File dbFile = new File(url);
        if (dbFile.exists()) {
            dbFile.delete();
            System.out.println("Database successfully deleted.");
        } else {
            System.out.println("The database does not exist.");
        }
        conn = DBConnection.open_connection(url);
        SetDB.createDB();
    }

    @AfterAll
    static void tearDown() {
        DBConnection.disconnect();
    }

    @BeforeEach
    void clearAllTable() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DBConnection.open_connection(url);
        }

        conn.setAutoCommit(true);

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = OFF;");

            List<String> tables = Arrays.asList(
                    "User", "Community", "UserAccess", "Admin", "Rules",
                    "Post", "Comment", "CommentHierarchy", "BannedUsers",
                    "Moderator", "Subscription", "PostVotes", "PostWarnings",
                    "CommentWarnings"
            );

            for (String t : tables) {
                stmt.executeUpdate("DELETE FROM " + t + ";");
            }

            // Resetta la sequenza per tutte le tabelle con AUTOINCREMENT
            stmt.executeUpdate("DELETE FROM sqlite_sequence;");

            stmt.executeUpdate("PRAGMA foreign_keys = ON;");
        }

        // Esegui VACUUM per ridurre lo spazio del database
        try (Statement vacuumStmt = conn.createStatement()) {
            vacuumStmt.executeUpdate("VACUUM;");
        }
    }

    // Test per aggiungere e rimuovere il like al commento
    @Test
    void AddRemovePostLike() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Creo Post
        POST_ID = postDAO.save(Map.of("title", POST_TITLE, "content", POST_CONTENT, "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // Inserisco il like al Post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleLike(user);

        // Controllare se il like è stato inserito
        assertEquals(LIKE, userDAO.getPostVote(USER_ID, POST_ID));

        // Rimuovo il like al Post
        postService.toggleLike(user);

        // Controllare se il like è stato rimosso
        assertEquals(null, userDAO.getPostVote(USER_ID, POST_ID));
    }

    // Test per aggiungere e rimuovere il dislike al post
    @Test
    void AddRemovePostDislike() throws SQLException {
        // creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", POST_TITLE, "content", POST_CONTENT, "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // inserisco il like al Post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleDislike(user);
        // controllare se il like è stato inserito
        assertEquals(DISLIKE, userDAO.getPostVote(USER_ID, POST_ID));

        // rimuovo il dislike al Post
        postService.toggleDislike(user);
        // controllare se il dislike è stato rimosso
        assertNull(userDAO.getPostVote(USER_ID, POST_ID));
    }

    // Test per il controllo del voto del post dell'utente
    @Test
    void checkUserVote() throws SQLException {
        // creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", POST_TITLE, "content", POST_CONTENT, "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // inserisco il like al Post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleLike(user);

        // controllo se il like è stato inserito
        assertTrue(postService.isLiked(USER_ID));
        assertFalse(postService.isDisliked(USER_ID));

        // inserisco il dislike e controllo se è stato inserito
        postService.toggleDislike(user);
        assertFalse(postService.isLiked(USER_ID));
        assertTrue(postService.isDisliked(USER_ID));
    }

    // Test per aggiungere e rimuovere il like al commento
    @Test
    void AddRemoveCommentLike() throws SQLException {
        // creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", POST_TITLE, "content", POST_CONTENT, "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // creo Comment
        COMMENT_ID = commentDAO.save(Map.of("post_id", POST_ID, "level", 0, "user_id", USER_ID, "content", "Test Content", "community_id", COMMUNITY_ID));

        // Inserisco il like al Comment
        User user = userDAO.findById(USER_ID).orElse(null);
        Comment comment = commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null);
        CommentService commentService = new CommentService(comment);
        commentService.toggleLike(user);
        // controllo se il like è stato inserito
        assertEquals(LIKE, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));

        // rimuovo il like al Comment
        commentService.toggleLike(user);
        // controllare se il like è stato rimosso
        assertNull(userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));
    }

    // Test per aggiungere e rimuovere il dislike al commento
    @Test
    void AddRemoveCommentDislike() throws SQLException {
        // creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", POST_TITLE, "content", POST_CONTENT, "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // creo Comment
        COMMENT_ID = commentDAO.save(Map.of("post_id", POST_ID, "level", 0, "user_id", USER_ID, "content", "Test Content", "community_id", COMMUNITY_ID));

        // Inserisco il like al comment
        User user = userDAO.findById(USER_ID).orElse(null);
        Comment comment = commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null);
        CommentService commentService = new CommentService(comment);
        commentService.toggleDislike(user);
        // controllare se il like è stato inserito
        assertEquals(DISLIKE, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));

        // Rimuovo il dislike al comment
        commentService.toggleDislike(user);
        // controllare se il dislike è stato rimosso
        assertNull(userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));
    }

    // Test per il controllo del voto del commento dell'utente
    @Test
    void checkUserVoteComment() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Creo Post
        POST_ID = postDAO.save(Map.of("title", POST_TITLE, "content", POST_CONTENT, "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // Creo Comment
        COMMENT_ID = commentDAO.save(Map.of("post_id", POST_ID, "level", 0, "user_id", USER_ID, "content", "Test Content", "community_id", COMMUNITY_ID));

        // Inserisco il like al comment
        User user = userDAO.findById(USER_ID).orElse(null);
        Comment comment = commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null);
        CommentService commentService = new CommentService(comment);
        commentService.toggleLike(user);

        // Controllo se il like è stato inserito
        assertTrue(commentService.isLiked(USER_ID));
        assertFalse(commentService.isDisliked(USER_ID));

        // inserisco il dislike e controllo se è stato inserito
        commentService.toggleDislike(user);
        assertFalse(commentService.isLiked(USER_ID));
        assertTrue(commentService.isDisliked(USER_ID));
    }


    // Test per la registrazione dell'utente
    @Test
    void registerUserTest() throws SQLException {
        // Creo User ma senza registrazione
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));

        // Eseguo login con utente non registrato
        assertFalse(userDAO.isValidUser(USER_NICKNAME, USER_PASSWORD));

        // Registrazione dell'utente
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);

        // Controllo se l'utente è stato registrato
        assertTrue(userDAO.isRegisteredUser(USER_NICKNAME));
    }

    // Test per l'iscrizione ad una community
    @Test
    void subscribeCommunityTest() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        User user = userDAO.findById(id).orElse(null);
        GuestContext.setCurrentGuest(user);

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Controllo se l'utente è iscritto alla community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        assertFalse(communityService.isSubscribed());

        // Iscrizione dell'utente alla community
        communityService.subscribe();
        assertTrue(communityService.isSubscribed());

        // L'utente si disiscrive dalla community
        communityService.unsubscribe();
        assertFalse(communityService.isSubscribed());
    }

    // Test per il controllo dell'utente bannato
    @Test
    void checkBannedUserTest() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        User user = userDAO.findById(id).orElse(null);
        GuestContext.setCurrentGuest(user);

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Controllo se l'utente è bannato dalla community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        assertFalse(communityService.checkBannedUser());

        // Banno l'utente dalla community
        communityService.banUser(id, "Reason");
        assertTrue(communityService.checkBannedUser());
    }

    // Test controllo moderator
    @Test
    void checkModeratorTest() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        int id = userDAO.getUserId(USER_NICKNAME);
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        User user = userDAO.findById(id).orElse(null);
        GuestContext.setCurrentGuest(user);

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Controllo se l'utente è un moderatore della community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        assertFalse(communityService.isModerator(id));

        // Nomino l'utente come moderatore della community
        communityService.promote(id);
        assertTrue(communityService.isModerator(id));

        // Rimuovo l'utente come moderatore della community
        communityService.dismiss(id);
        assertFalse(communityService.isModerator(id));
    }

//    // Test controllo admin
//    @Test
//    void checkAdminTest() throws SQLException {
//        // Creo User
//        userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
//        int id = userDAO.getUserId(USER_NICKNAME);
//        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
//        User user = userDAO.findById(id).orElse(null);
//        GuestContext guestContext = new GuestContext();
//        guestContext.setCurrentGuest(user);
//
//        // Controllo se l'utente è un admin
//        assertFalse(userDAO.isAdmin(id));
//
//        // Nomino l'utente come admin
//        userDAO.promoteAdmin(id);
//        assertTrue(userDAO.isAdmin(id));
//
//        // Rimuovo l'utente come admin
//        userDAO.dismissAdmin(id);
//        assertFalse(userDAO.isAdmin(id));
//    }


}
