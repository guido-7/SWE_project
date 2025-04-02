package test;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import src.controllers.pagecontrollers.HomePageController;
import src.controllers.pagecontrollers.PostCreationPageController;
import src.usersession.GuestContext;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class UITestUtils extends ApplicationTest {
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
        TextField usernameField = lookup("#usernameField").query();
        TextField passwordField = lookup("#passwordField").query();
        Button loginButton = lookup("#loginButton").query();

        Platform.runLater(() -> {
            usernameField.setText(username);
            passwordField.setText(password);
            loginButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);
    }

    public void register(String name, String surname, String nickname, String password) {
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
        sleep(500);
    }

    public void openCommunityPage(String communityTitle) throws Exception {
        TextField searchField = lookup("#searchField").query();
        Platform.runLater(() -> {
            searchField.fireEvent(mouseClick);
            searchField.setText(communityTitle);
        });
        WaitForAsyncUtils.waitForFxEvents();
        sleep(150);

        HomePageController homePageController = (HomePageController) GuestContext.getCurrentController();
        ContextMenu contextMenu = getPrivateField(homePageController.getCommunitySearchHelper(), "suggestionsPopup");
        CustomMenuItem firstItem = (CustomMenuItem) contextMenu.getItems().getFirst();
        Platform.runLater(firstItem::fire);
        WaitForAsyncUtils.waitForFxEvents();
        sleep(500);
    }

    public void subscribeCommunity(String communityTitle) throws Exception {
        openCommunityPage(communityTitle);
        Button subscribeButton = lookup("#subscribeButton").query();
        if(subscribeButton.isVisible()) {
            Platform.runLater(() -> {
                subscribeButton.fireEvent(mouseClick);
            });
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void unsubscribeCommunity(String communityTitle) throws Exception {
        openCommunityPage(communityTitle);
        Button unsubscribeButton = lookup("#unsubscribeButton").query();
        if(unsubscribeButton.isVisible()) {
            Platform.runLater(() -> {
                unsubscribeButton.fireEvent(mouseClick);
            });
            WaitForAsyncUtils.waitForFxEvents();
        }
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

    public void openPost() {
        VBox post = getFirstPost();
        Button postPage = from(post).lookup("#postButton").query();
        Platform.runLater(postPage::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    //----- interaction with posts -----

    public void like(VBox post) {
        Button likeButton = from(post).lookup("#likeButton").query();
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dislike(VBox post) {
        Button likeButton = from(post).lookup("#dislikeButton").query();
        Platform.runLater(likeButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pinPost(VBox post) {
        Button pinButton = from(post).lookup("#pinPostButton").query();
        Platform.runLater(() -> pinButton.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void deletePost(VBox post) {
        ImageView deleteButton = from(post).lookup("#deletePostButton").query();
        Platform.runLater(() -> deleteButton.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public VBox getFirstPost() {
        VBox postsContainer = lookup("#postsContainer").query();
        if (postsContainer.getChildren().isEmpty()) {
            System.out.println("No posts found");
            return null;
        }
        return (VBox) postsContainer.getChildren().getFirst();
    }

    public void pressButton(String buttonId) {
        Button button = lookup(buttonId + "").query();
        Platform.runLater(() -> button.fireEvent(mouseClick));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(200);
    }

    public <T> T getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }
}
