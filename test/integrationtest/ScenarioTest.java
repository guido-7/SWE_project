package test.integrationtest;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import src.controllers.HomePageController;
import src.controllers.factory.PageControllerFactory;
import src.domainmodel.Guest;
import src.domainmodel.PermitsManager;
import src.domainmodel.Role;
import src.domainmodel.User;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.orm.CommunityDAO;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;
import test.UITestUtils;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ScenarioTest  extends ApplicationTest {
    UITestUtils uiTestUtils = new UITestUtils();
    CommunityDAO communityDAO = new CommunityDAO();
    public static Connection connection;
    private static String url = "database/bigDBTest.db";

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        Platform.runLater(() -> {
            try {
                initializeApplication(new Stage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        WaitForAsyncUtils.waitForFxEvents(); // Attendi che l'azione venga eseguita
        connection = DBConnection.open_connection(url);
    }

    @BeforeAll
    public static void seDB() throws SQLException {
        DBConnection.changeDBPath(url);
        File dbFile = new File(url);
        if (dbFile.exists()) {
            Boolean isDeleted = dbFile.delete();
            if(isDeleted)
                System.out.println("Database deleted successfully");
            else
                System.out.println("Database not deleted successfully");
            System.out.println("Database successfully deleted.");
        } else {
            System.out.println("The database does not exist.");
        }
        connection = DBConnection.open_connection(url);
        SetDB.createDB();
        SetDB.generatefakedata(40, 10, 100, 40);
    }

    @Test
    public void createCommunityAndPinPost() throws Exception {
        uiTestUtils.goToLoginPage();
        uiTestUtils.login("admin", "12345678");

        // create community with rules
        String comTitle = "Sport";
        String comDescription = "Community about sport";
        ArrayList<Pair<String, String>> rules = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            rules.add(new Pair<>("title" + i, "content" + i));
        }

        uiTestUtils.createCommunity(comTitle, comDescription, rules);
        assertTrue(communityDAO.findByTitle(comTitle));

        // open community page
        uiTestUtils.openCommunityPage(comTitle);
        Text CommunityTitle = lookup("#community_title").query();
        assertEquals(comTitle, CommunityTitle.getText());

        // verify there aren't posts
        VBox postsContainer = lookup("#postsContainer").query();
        assertTrue(postsContainer.getChildren().isEmpty());

        //crea post
        String title = "Post title";
        String content = "Post content";
        uiTestUtils.createPost(comTitle, title, content);
        sleep(500);

        // verify that the first post is the one just created
        VBox post = uiTestUtils.getFirstPost();
        assertNotNull(post);
        Label postTitle = from(post).lookup("#title").queryAs(Label.class);
        Label postContent = from(post).lookup("#content").queryAs(Label.class);
        assertEquals(title, postTitle.getText());
        assertEquals(content, postContent.getText());

        connection = DBConnection.open_connection();
        String query = "SELECT * FROM Post WHERE title = ? AND content = ?";
        PreparedStatement stm = connection.prepareStatement(query);
        stm.setString(1, title);
        stm.setString(2, content);
        ResultSet rs = stm.executeQuery();
        assertNotNull(rs);

        assertEquals(title, rs.getString("title"));
        assertEquals(content, rs.getString("content"));
        int idPost = rs.getInt("id");

        //pin post
        uiTestUtils.pinPost(post);

        connection = DBConnection.open_connection();
        query = "SELECT * FROM PinnedPost JOIN Post ON PinnedPost.post_id = Post.id WHERE id = ?";
        stm = connection.prepareStatement(query);
        stm.setInt(1, idPost);
        rs = stm.executeQuery();
        assertNotNull(rs);
        assertEquals(title, rs.getString("title"));
        assertEquals(content, rs.getString("content"));

        Button pinButton = from(post).lookup("#pinPostButton").query();
        ImageView image = (ImageView) pinButton.getGraphic();
        String urlPinPost = image.getImage().getUrl();
        urlPinPost = urlPinPost.substring(urlPinPost.indexOf("src"));
        assertEquals("src/view/images/PinClickIcon.png", urlPinPost);

        //verifica che il post sia stato pinnato nel box
        VBox pinnedPost = lookup("#pinnedPostsContainer").query();
        assertFalse(pinnedPost.getChildren().isEmpty());
        pinnedPost.getChildren().getFirst();
        Label postPinTitle = lookup("#postTitle").queryAs(Label.class);
        assertEquals(title, postPinTitle.getText());
    }

    @Test
    public void registerAndLoginAndLikePost() throws Exception {
        String name = "Luca";
        String surname = "Bianchi";
        String nickname = "LucaBianchi";
        String password = "qwerty1234";

        uiTestUtils.goToRegisterPage();
        uiTestUtils.register(name, surname, nickname, password);

        String isUserQuery = "SELECT id FROM main.User  WHERE name = ? AND surname = ? AND nickname = ?";
        connection = DBConnection.open_connection();
        PreparedStatement stm = connection.prepareStatement(isUserQuery);
        stm.setString(1,name);
        stm.setString(2, surname);
        stm.setString(3, nickname);
        ResultSet rs = stm.executeQuery();
        assertNotNull(rs);
        assertTrue(rs.next());
        int userId = rs.getInt("id");

        String isSensitiveInfoSavedQuery ="SELECT * FROM main.UserAccess WHERE email = ? AND password = ?";
        stm = connection.prepareStatement(isSensitiveInfoSavedQuery);
        stm.setString(1, nickname);
        stm.setString(2, password);
        rs = stm.executeQuery();
        assertNotNull(rs);
        assertTrue(rs.next());

        uiTestUtils.login(nickname, password);
        int currentId = ((User) GuestContext.getCurrentGuest()).getId();
        assertEquals(currentId, userId);

        //metti like a aggiorni la UI
        //verificare se il bottone like è verde
        VBox post = uiTestUtils.getFirstPost();
        uiTestUtils.like(post);
        sleep(2000);
        Label title = from(post).lookup("#title").queryAs(Label.class);
        Label content = from(post).lookup("#content").queryAs(Label.class);
        Button likeButton = from(post).lookup("#likeButton").queryAs(Button.class);
        boolean isGreen = likeButton.getStyleClass().contains("selected");
        assertTrue(isGreen);

        String isLiked ="SELECT * FROM Post JOIN main.PostVotes WHERE title = ? AND content = ? AND vote_type = 1";
        connection = DBConnection.open_connection();
        stm = connection.prepareStatement(isLiked);
        stm.setString(1,title.getText());
        stm.setString(2, content.getText());
        rs = stm.executeQuery();
        assertNotNull(rs);
        assertTrue(rs.next());
    }

    private void initializeApplication(Stage stage) throws IOException {
        Guest guest = new Guest(PermitsManager.createGuestPermits(), Role.GUEST);
        GuestContext.setCurrentGuest(guest);
        SceneManager.setPrimaryStage(stage);
        HomePageController homePageController = PageControllerFactory.createHomePageController(guest);
        SceneManager.loadPrimaryScene("home", "/src/view/fxml/HomePage.fxml", homePageController);
    }
}
