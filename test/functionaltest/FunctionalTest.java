package test.functionaltest;

import javafx.application.Platform;

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import src.controllers.*;
import src.controllers.factory.PageControllerFactory;
import src.domainmodel.Guest;
import src.domainmodel.PermitsManager;
import src.domainmodel.Role;
import src.managerdatabase.DBConnection;
import src.managerdatabase.SetDB;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalTest extends ApplicationTest {
    private HomePageController homePageController;

    MouseEvent mouseClick = new MouseEvent(
            MouseEvent.MOUSE_CLICKED,   // Tipo di evento
            0,                         // Coordinate X
            0,                         // Coordinate Y
            0,                         // Coordinate screenX
            0,                         // Coordinate screenY
            MouseButton.PRIMARY,       // Tipo di tasto (primo tasto del mouse)
            1,                         // Numero di clic (ad esempio 1 per il clic singolo)
            false,                     // Shift key premuto
            false,                     // Control key premuto
            false,                     // Alt key premuto
            false,                     // Meta key premuto
            false,                     // Primary button down
            false,                     // Middle button down
            false,                     // Secondary button down
            false,                     // Back button down
            false,                     // Forward button down
            false,                     // Synthesized
            false,                     // Popup trigger
            false,                     // Still since press
            null                       // PickResult (puoi lasciare null, se non lo utilizzi)
    );

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
    }

    @BeforeAll
    public static void seDB() throws SQLException {
        String url = "database/bigDBTest.db";
        //DBConnection.changeDBPath(url);
        File dbFile = new File(url);
        if (dbFile.exists()) {
            dbFile.delete();
            System.out.println("Database successfully deleted.");
        } else {
            System.out.println("The database does not exist.");
        }
        Connection conn = DBConnection.open_connection(url);
        SetDB.createDB();
        SetDB.generatefakedata(40, 10, 100, 40);
    }

    @Test
    void testVisibilityGuest() throws Exception {
        ImageView userProfileAccess = getPrivateField(homePageController, "userProfileAccess");
        Button createCommunityButton = getPrivateField(homePageController, "createCommunityButton");
        Button login = getPrivateField(homePageController, "login");

        // Test visibilità iniziale degli elementi
        assertFalse(userProfileAccess.isVisible());
        assertFalse(createCommunityButton.isVisible());
        assertTrue(login.isVisible());

        // Test proprietà managed degli elementi
        assertFalse(userProfileAccess.isManaged());
        assertFalse(createCommunityButton.isManaged());
        assertTrue(login.isManaged());
    }

    @Test
    void testGoToLoginPage() throws Exception {
        goToLoginPage();

        assertEquals("login", SceneManager.getCurrentStageName());
    }

    @Test
    void testLogin() throws Exception {
        login();

        assertEquals("home", SceneManager.getCurrentStageName());
    }

    @Test
    void testSubscribeCommunity() throws Exception {
        login();
        subscribeCommunity("news");

        Text communityTitle = lookup("#community_title").queryAs(Text.class);
        String titleText = communityTitle.getText();
        assertEquals("news", titleText);

        assertTrue(lookup("#unsubscribeButton").query().isVisible());
        assertFalse(lookup("#subscribeButton").query().isVisible());
    }

    @Test
    void testOpenPost() {
        openPost();

        assertInstanceOf(PostPageController.class, GuestContext.getCurrentController());
    }

    @Test
    void testLikePost() throws Exception {
        login();
        openPost();

        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button likeButton = from(post).lookup("#likeButton").query();
        Label likeCount = from(post).lookup("#scoreLabel").query();
        int initialLikes = Integer.parseInt(likeCount.getText());

        // add like
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        int finalLikes = Integer.parseInt(likeCount.getText());
        assertEquals(initialLikes + 1, finalLikes);

        // remove like
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testDislikePost() throws Exception {
        login();
        openPost();

        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button dislikeButton = from(post).lookup("#dislikeButton").query();
        Label dislikeCount = from(post).lookup("#scoreLabel").query();
        int initialDislikes = Integer.parseInt(dislikeCount.getText());

        // add dislike
        Platform.runLater(dislikeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        int finalDislikes = Integer.parseInt(dislikeCount.getText());
        assertEquals(initialDislikes - 1, finalDislikes);

        // remove dislike
        Platform.runLater(dislikeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testLikeByGuest() {
        openPost();

        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button likeButton = from(post).lookup("#likeButton").query();
        Label likeCount = from(post).lookup("#scoreLabel").query();
        int initialLikes = Integer.parseInt(likeCount.getText());

        // add like
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        int finalLikes = Integer.parseInt(likeCount.getText());
        assertEquals(initialLikes, finalLikes);

        // remove like
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testComment() throws Exception {
        login();
        openPost();

        // open reply field
        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button openReplyButton = from(post).lookup("#postButton").query();
        Platform.runLater(openReplyButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        TextArea replyText = from(post).lookup("#replyField").query();
        Button replyButton = from(post).lookup("#sendButton").query();
        Platform.runLater(() -> {
            replyText.setText("Test comment");
            replyButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
        //sleep(5000);

        VBox reply = (VBox) postsContainer.getChildren().getLast();
        Label replyTextElement = from(reply).lookup("#content").query();
        assertEquals("Test comment", replyTextElement.getText());
    }

    @Test
    void testCreateCommunity() throws Exception {
        login();
        pressButton("#createCommunityButton");

        TextField titleField = lookup("#titleField").query();
        TextArea descriptionField = lookup("#descriptionArea").query();
        TextField ruleTitle1 = lookup("#RuleTitle1").query();
        TextArea ruleDescription1 = lookup("#rule1").query();
        TextField ruleTitle2 = lookup("#RuleTitle2").query();
        TextArea ruleDescription2 = lookup("#rule2").query();
        TextField ruleTitle3 = lookup("#RuleTitle3").query();
        TextArea ruleDescription3 = lookup("#rule3").query();
        Button createButton = lookup("#createButton").query();
        Platform.runLater(() -> {
            titleField.setText("Test Community");
            descriptionField.setText("Test description");
            ruleTitle1.setText("Test rule 1");
            ruleDescription1.setText("Test rule description 1");
            ruleTitle2.setText("Test rule 2");
            ruleDescription2.setText("Test rule description 2");
            ruleTitle3.setText("Test rule 3");
            ruleDescription3.setText("Test rule description 3");
            createButton.fireEvent(mouseClick);
        });
        WaitForAsyncUtils.waitForFxEvents();

        openCommunityPage("Test Community");

        Text communityTitle = lookup("#community_title").queryAs(Text.class);
        assertEquals("Test Community", communityTitle.getText());
    }

    @Test
    void testCreatePost() throws Exception {
        login();
        subscribeCommunity("news");
        sleep(5000);
        //openCommunityCreationPage();
        pressButton("#createPostButton");
        sleep(5000);

        TextField community = lookup("#communitySearchBar").query();
        TextField titleField = lookup("#titleField").query();
        TextArea contentField = lookup("#contentArea").query();
        Button createButton = lookup("#postButton").query();
        Platform.runLater(() -> {
            community.setText("News");
            titleField.setText("Test Post");
            contentField.setText("Test content");
            createButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        openPost();

        Text postTitle = lookup("#postTitle").queryAs(Text.class);
        assertEquals("Test Post", postTitle.getText());
    }

    private void initializeApplication(Stage stage) throws IOException {
        Guest guest = new Guest(PermitsManager.createGuestPermits(), Role.GUEST);
        GuestContext.setCurrentGuest(guest);
        SceneManager.setPrimaryStage(stage);
        this.homePageController = PageControllerFactory.createHomePageController(guest);
        SceneManager.loadPrimaryScene("home", "/src/view/fxml/HomePage.fxml", homePageController);
    }

    private <T> T getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    void goToLoginPage() throws Exception {
        Button login = lookup("#login").query();
        Platform.runLater(() -> login.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

    void login() throws Exception {
        goToLoginPage();
        TextField usernameField = lookup("#usernameField").query();
        TextField passwordField = lookup("#passwordField").query();
        Button loginButton = lookup("#loginButton").query();

        Platform.runLater(() -> {
            usernameField.setText("admin");
            passwordField.setText("12345678");
            loginButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    void openCommunityPage(String communityTitle) throws Exception {
        TextField searchField = lookup("#searchField").query();
        Platform.runLater(() -> {
            searchField.setText(communityTitle);
        });
        WaitForAsyncUtils.waitForFxEvents();

        ContextMenu contextMenu = getPrivateField(homePageController.getCommunitySearchHelper(), "suggestionsPopup");
        CustomMenuItem firstItem = (CustomMenuItem) contextMenu.getItems().getFirst();
        Platform.runLater(firstItem::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    void subscribeCommunity(String communityTitle) throws Exception {
        openCommunityPage(communityTitle);
        Button subscribeButton = lookup("#subscribeButton").query();
        if(subscribeButton.isVisible()) {
            Platform.runLater(() -> {
                subscribeButton.fireEvent(mouseClick);
            });
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    void pressButton(String buttonId) {
        Button button = lookup(buttonId + "").query();
        //Platform.runLater(button::fire);
        Platform.runLater(() -> button.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

    void openPost() {
        VBox postsContainer = lookup("#postsContainer").query();
        assertFalse(postsContainer.getChildren().isEmpty());
        VBox post = (VBox) postsContainer.getChildren().getFirst();
        Button postPage = from(post).lookup("#postButton").query();
        Platform.runLater(postPage::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

}
