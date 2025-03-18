package test.integrationtest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.businesslogic.CommentService;
import src.businesslogic.PostService;
import src.domainmodel.Comment;
import src.domainmodel.User;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommentDAO;
import src.orm.CommunityDAO;
import src.orm.PostDAO;
import src.orm.UserDAO;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    //DAO
    PostDAO postDAO = new PostDAO();
    CommentDAO commentDAO = new CommentDAO();
    CommunityDAO communityDAO = new CommunityDAO();
    UserDAO userDAO = new UserDAO();


    @BeforeAll
    static void setUp(){
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
        // creo User
        userDAO.save(Map.of("nickname", "user_test", "name", "Giacomo", "surname", "Rossi"));
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        //creo Community
        communityDAO.save(Map.of("title", "Test Community", "description", "Test Description"));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", "Test Title", "content", "Test Content", "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // Inserisco il like al post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleLike(user);
        // controllare se il like è stato inserito
        assertEquals(1, userDAO.getPostVote(USER_ID, POST_ID));

        // Rimuovo il like al post
        postService.toggleLike(user);
        // controllare se il like è stato rimosso
        assertEquals(null, userDAO.getPostVote(USER_ID, POST_ID));
    }

    // Test per aggiungere e rimuovere il dislike al post
    @Test
    void AddRemovePostDislike() throws SQLException {
        // creo User
        userDAO.save(Map.of("nickname", "user_test", "name", "Giacomo", "surname", "Rossi"));
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        //creo Community
        communityDAO.save(Map.of("title", "Test Community", "description", "Test Description"));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", "Test Title", "content", "Test Content", "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // Inserisco il like al post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleDislike(user);
        // controllare se il like è stato inserito
        assertEquals(0, userDAO.getPostVote(USER_ID, POST_ID));

        // Rimuovo il dislike al post
        postService.toggleDislike(user);
        // controllare se il dislike è stato rimosso
        assertEquals(null, userDAO.getPostVote(USER_ID, POST_ID));
    }

    // Test per il controllo del voto del post dell'utente
    @Test
    void checkUserVote() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", "user_test", "name", "Giacomo", "surname", "Rossi"));
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        // Creo Community
        communityDAO.save(Map.of("title", "Test Community", "description", "Test Description"));

        // Creo Post
        POST_ID = postDAO.save(Map.of("title", "Test Title", "content", "Test Content", "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // Inserisco il like al post
        User user = userDAO.findById(USER_ID).orElse(null);
        PostService postService = new PostService(postDAO.findById(POST_ID).orElse(null));
        postService.toggleLike(user);

        // Controllo se il like è stato inserito
        assertEquals(true, postService.isLiked(user.getId()));
        assertEquals(false, postService.isDisliked(user.getId()));

        // inserisco il dislike e controllo se è stato inserito
        postService.toggleDislike(user);
        assertEquals(false, postService.isLiked(user.getId()));
        assertEquals(true, postService.isDisliked(user.getId()));
    }

    // Test per aggiungere e rimuovere il like al commento
    @Test
    void AddRemoveCommentLike() throws SQLException {
        // creao User
        userDAO.save(Map.of("nickname", "user_test", "name", "Giacomo", "surname", "Rossi"));
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        //creo Community
        communityDAO.save(Map.of("title", "Test Community", "description", "Test Description"));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", "Test Title", "content", "Test Content", "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // creo Comment
        COMMENT_ID = commentDAO.save(Map.of("post_id", POST_ID, "level",0, "user_id", USER_ID, "content", "Test Content","community_id", COMMUNITY_ID));

        // Inserisco il like al comment
        User user = userDAO.findById(USER_ID).orElse(null);
        Comment comment = commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null);
        CommentService commentService = new CommentService(comment);
        commentService.toggleLike(user);
        // controllare se il like è stato inserito
        assertEquals(1, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));

        // Rimuovo il like al comment
        commentService.toggleLike(user);
        // controllare se il like è stato rimosso
        assertEquals(null, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));
    }

    // Test per aggiungere e rimuovere il dislike al commento
    @Test
    void AddRemoveCommentDislike() throws SQLException {
        // creao User
        userDAO.save(Map.of("nickname", "user_test", "name", "Giacomo", "surname", "Rossi"));
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        //creo Community
        communityDAO.save(Map.of("title", "Test Community", "description", "Test Description"));

        // creo Post
        POST_ID = postDAO.save(Map.of("title", "Test Title", "content", "Test Content", "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // creo Comment
        COMMENT_ID = commentDAO.save(Map.of("post_id", POST_ID, "level",0, "user_id", USER_ID, "content", "Test Content","community_id", COMMUNITY_ID));

        // Inserisco il like al comment
        User user = userDAO.findById(USER_ID).orElse(null);
        Comment comment = commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null);
        CommentService commentService = new CommentService(comment);
        commentService.toggleDislike(user);
        // controllare se il like è stato inserito
        assertEquals(0, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));

        // Rimuovo il dislike al comment
        commentService.toggleDislike(user);
        // controllare se il dislike è stato rimosso
        assertEquals(null, userDAO.getCommentVote(USER_ID, COMMENT_ID, POST_ID));
    }

    // Test per il controllo del voto del commento dell'utente
    @Test
    void checkUserVoteComment() throws SQLException {
        // Creo User
        userDAO.save(Map.of("nickname", "user_test", "name", "Giacomo", "surname", "Rossi"));
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        // Creo Community
        communityDAO.save(Map.of("title", "Test Community", "description", "Test Description"));

        // Creo Post
        POST_ID = postDAO.save(Map.of("title", "Test Title", "content", "Test Content", "community_id", COMMUNITY_ID, "user_id", USER_ID));

        // Creo Comment
        COMMENT_ID = commentDAO.save(Map.of("post_id", POST_ID, "level",0, "user_id", USER_ID, "content", "Test Content","community_id", COMMUNITY_ID));

        // Inserisco il like al comment
        User user = userDAO.findById(USER_ID).orElse(null);
        Comment comment = commentDAO.findById(List.of(POST_ID, COMMENT_ID)).orElse(null);
        CommentService commentService = new CommentService(comment);
        commentService.toggleLike(user);

        // Controllo se il like è stato inserito
        assertEquals(true, commentService.isLiked(user.getId()));
        assertEquals(false, commentService.isDisliked(user.getId()));

        // inserisco il dislike e controllo se è stato inserito
        commentService.toggleDislike(user);
        assertEquals(false, commentService.isLiked(user.getId()));
        assertEquals(true, commentService.isDisliked(user.getId()));
    }

    // Test per la registrazione dell'utente
    @Test
    void registerUserTest() throws SQLException {
        // Creo User ma senza registrazione
        userDAO.save(Map.of("nickname", "user_test", "name", "Luigi", "surname", "Bianchi"));

        // Eseguo login con utente non registrato
        assertEquals(false, userDAO.isValidUser("user_test", "12345678"));

        // Registrazione dell'utente
        int id = userDAO.getUserId("user_test");
        userDAO.registerUserAccessInfo(id, "user_test", "12345678");

        // Controllo se l'utente è stato registrato
        assertEquals(true, userDAO.isRegisteredUser("user_test"));
    }





}
