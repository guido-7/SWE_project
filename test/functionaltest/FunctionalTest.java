package test.functionaltest;

import javafx.application.Platform;

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import src.businesslogic.FeedService;
import src.controllers.*;
import src.domainmodel.Guest;
import src.domainmodel.PermitsManager;
import src.domainmodel.Role;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;

import java.io.IOException;
import java.lang.reflect.Field;

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
    public void start(Stage stage) throws IOException {
        //initializeApplication(stage);
    }

    @BeforeEach
    void setup() {
        Platform.runLater(() -> {
            try {
                initializeApplication(new Stage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        WaitForAsyncUtils.waitForFxEvents(); // Attendi che l'azione venga eseguita
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

    private void initializeApplication(Stage stage) throws IOException {
        Guest guest = new Guest(PermitsManager.createGuestPermits(), Role.GUEST);
        GuestContext.setCurrentGuest(guest);
        SceneManager.setPrimaryStage(stage);
        this.homePageController = new HomePageController(new FeedService(guest));
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

    void goToCommunityPage(String communityTitle) throws Exception {
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
        goToCommunityPage(communityTitle);
        Button subscribeButton = lookup("#subscribeButton").query();
        if(subscribeButton.isVisible()) {
            Platform.runLater(() -> {
                subscribeButton.fireEvent(mouseClick);
            });
        }
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
