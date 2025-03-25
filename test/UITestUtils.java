package test;

import javafx.application.Platform;

import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import src.controllers.HomePageController;
import src.controllers.PostCreationPageController;
import src.domainmodel.Community;
import src.domainmodel.Post;
import src.orm.CommunityDAO;
import src.servicemanager.GuestContext;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UITestUtils extends ApplicationTest {
    CommunityDAO communityDAO = new CommunityDAO();

    public static MouseEvent mouseClick = new MouseEvent(
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

    public void goToLoginPage() throws Exception {
        Button login = lookup("#login").query();
        Platform.runLater(() -> login.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void goToRegisterPage() throws Exception {
        goToLoginPage();
        Hyperlink register = lookup("#signUp").query();
        Platform.runLater(register::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void login(String username, String password) throws Exception {
        //goToLoginPage();
        TextField usernameField = lookup("#usernameField").query();
        TextField passwordField = lookup("#passwordField").query();
        Button loginButton = lookup("#loginButton").query();

        Platform.runLater(() -> {
            usernameField.setText(username);
            passwordField.setText(password);
            loginButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void register(String name, String surname, String nickname, String password) throws Exception {
        TextField nameField = lookup("#name").query();
        TextField surnameField = lookup("#surname").query();
        TextField nicknameField = lookup("#nickname").query();
        TextField passwordField = lookup("#password_id").query();
        Button registerButton = lookup("#SignUpButton").query();

        Platform.runLater(() -> {
            nameField.setText(name);
            surnameField.setText(surname);
            nicknameField.setText(nickname);
            passwordField.setText(password);
            registerButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        //pressButton("#SignUpButton");
    }

    public void createCommunity(String title, String description, ArrayList<Pair<String, String>> rules) throws Exception {
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
            titleField.setText(title);
            descriptionField.setText(description);
            ruleTitle1.setText(rules.getFirst().getKey());
            ruleDescription1.setText(rules.getFirst().getValue());
            ruleTitle2.setText(rules.get(1).getKey());
            ruleDescription2.setText(rules.get(1).getValue());
            ruleTitle3.setText(rules.get(2).getKey());
            ruleDescription3.setText(rules.get(2).getValue());
            createButton.fireEvent(mouseClick);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openCommunityPage(String communityTitle) throws Exception {
        TextField searchField = lookup("#searchField").query();
        Platform.runLater(() -> searchField.setText(communityTitle));
        WaitForAsyncUtils.waitForFxEvents();

        HomePageController homePageController = (HomePageController) GuestContext.getCurrentController();
        ContextMenu contextMenu = getPrivateField(homePageController.getCommunitySearchHelper(), "suggestionsPopup");
        CustomMenuItem firstItem = (CustomMenuItem) contextMenu.getItems().getFirst();
        Platform.runLater(firstItem::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void createPost(String comTitle, String title, String content) throws Exception {
        pressButton("#createPostButton");

        // select community
        TextField communityField = lookup("#communitySearchBar").query();
        Platform.runLater(() -> communityField.setText(comTitle));
        WaitForAsyncUtils.waitForFxEvents();

        //select first suggestion
        PostCreationPageController postCreationPageController = (PostCreationPageController) GuestContext.getCurrentController();
        ContextMenu contextMenu = getPrivateField(postCreationPageController.getCommunitySearchHelper(), "suggestionsPopup");
        CustomMenuItem firstItem = (CustomMenuItem) contextMenu.getItems().getFirst();
        Platform.runLater(firstItem::fire);
        WaitForAsyncUtils.waitForFxEvents();

        TextField titleField = lookup("#titleField").query();
        TextArea contentField = lookup("#contentArea").query();
        Platform.runLater(() -> {
            titleField.setText(title);
            contentField.setText(content);
        });
        WaitForAsyncUtils.waitForFxEvents();

        pressButton("#postButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pinPost(VBox post) {
        Button pinButton = from(post).lookup("#pinPostButton").query();
        Platform.runLater(() -> pinButton.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void like(VBox post) {
        Button likeButton = from(post).lookup("#likeButton").query();
        Platform.runLater(() -> likeButton.fire());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public VBox getFirstPost() {
        VBox postsContainer = lookup("#postsContainer").query();
        if (postsContainer.getChildren().isEmpty()) {
            return null;
        }
        return (VBox) postsContainer.getChildren().getFirst();
    }

    private <T> T getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private void pressButton(String buttonId) {
        Button button = lookup(buttonId + "").query();
        //Platform.runLater(button::fire);
        Platform.runLater(() -> button.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

}
