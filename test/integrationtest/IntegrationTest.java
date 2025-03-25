package test.integrationtest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.businesslogic.*;
import src.domainmodel.Comment;
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
    AdminDAO adminDAO = new AdminDAO();

    @BeforeAll
    static void setUp() {
        DBConnection.changeDBPath(url);
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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
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
        assertNull(userDAO.getPostVote(USER_ID, POST_ID));
    }

    // Test per aggiungere e rimuovere il dislike al post
    @Test
    void AddRemovePostDislike() throws SQLException {
        // creo User
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Controllo se l'utente è iscritto alla community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        assertFalse(communityService.isSubscribed());

        // Iscrizione dell'utente alla community
        assertTrue(communityService.subscribe());
        assertTrue(communityService.isSubscribed());

        // L'utente si disiscrive dalla community
        assertTrue(communityService.unsubscribe());
        assertFalse(communityService.isSubscribed());
    }

    // Test per il controllo dell'utente bannato
    @Test
    void checkBannedUserTest() throws SQLException {
        // Creo User
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

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
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Controllo se l'utente è un moderatore della community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        assertFalse(communityService.isModerator(id));

        // Nomino l'utente come moderatore della community
        communityService.promoteToModerator(id);
        assertTrue(communityService.isModerator(id));

        // Rimuovo l'utente come moderatore della community
        communityService.downgradeModerator(id);
        assertFalse(communityService.isModerator(id));
    }

    // Test controllo admin
    @Test
    void checkAdminTest() throws SQLException {
        // Creo User
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Controllo se l'utente è un admin della community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        assertNull(communityService.getAdmin(id));

        // Nomino l'utente come admin
        communityService.promoteToAdmin(id);
        assertTrue(communityService.isAdmin(id));

        // Rimuovo l'utente come admin della community
        communityService.downgradeAdmin(id);
        assertFalse(communityService.isAdmin(id));
    }

    // Test per la creazione ed eliminazione di una community
    @Test
    void createDeleteCommunityTest() throws SQLException {
        // Creo User
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

        // Creo Community
        CommunityCreationService communityCreationService = new CommunityCreationService();

        // Controllo se la community è stata creata
        assertEquals(COMMUNITY_ID,communityCreationService.createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));
        assertNotNull(communityDAO.findById(COMMUNITY_ID).orElse(null));

        // Elimino la community
        CommunityService communityService = new CommunityService(COMMUNITY_ID);
        communityService.deleteCommunity();

        // Controllo se la community è stata eliminata
        assertNull(communityDAO.findById(COMMUNITY_ID).orElse(null));
    }

    // Test per la creazione di un post
    @Test
    void createDeletePostTest() throws SQLException {
        // Creo User
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

        // Creo Community
        communityDAO.save(Map.of("title", COMMUNITY_TITLE, "description", COMMUNITY_DESCRIPTION));

        // Creo Post
        PostCreationService postCreationService = new PostCreationService();
        postCreationService.createPost(COMMUNITY_TITLE, POST_TITLE, POST_CONTENT, COMMUNITY_ID, id);

        // Controllo se il post è stato creato
        assertNotNull(postDAO.findById(POST_ID).orElse(null));
        assertEquals(POST_ID, postDAO.findById(POST_ID).orElse(null).getId());

        // Elimino il post
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.deletePost(POST_ID);

        // Controllo se il post è stato eliminato
        assertNull(postDAO.findById(POST_ID).orElse(null));
    }

    // Test per creare community già esistente
    @Test
    void createExistingCommunity() throws SQLException {
        // Creo User
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        GuestContext.setCurrentGuest(userDAO.findById(id).orElse(null));

        // Creo Community
        CommunityCreationService communityCreationService = new CommunityCreationService();

        // Controllo se la community è stata creata
        assertEquals(COMMUNITY_ID,communityCreationService.createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));
        assertNotNull(communityDAO.findById(COMMUNITY_ID).orElse(null));

        //todo da finire
        // Provo a ricrearla
        //assertThrows(SQLException.class, () -> communityCreationService.createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));
        //assertEquals(-1,communityCreationService.createCommunity(COMMUNITY_TITLE, COMMUNITY_DESCRIPTION));

    }

    private int createUser() throws SQLException {
        int id = userDAO.save(Map.of("nickname", USER_NICKNAME, "name", USER_NAME, "surname", USER_SURNAME));
        userDAO.registerUserAccessInfo(id, USER_NICKNAME, USER_PASSWORD);
        return id;
    }
}
