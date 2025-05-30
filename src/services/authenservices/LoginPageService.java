package src.services.authenservices;

import javafx.scene.control.Label;
import src.factory.PageControllerFactory;
import src.services.FeedService;
import src.controllers.pagecontrollers.HomePageController;
import src.domainmodel.User;
import src.persistence.DAOs.UserDAO;
import src.usersession.GuestContext;
import src.usersession.SceneManager;

import java.sql.SQLException;

public class LoginPageService {
    private final UserDAO userDAO = new UserDAO();

    public void manageLogin(String username, String password, Label errorLabel) {
        String validationMessage = validateInput(username, password);

        if (validationMessage != null) {
            showError(errorLabel, validationMessage);
            return;
        }

        try {
            if (!userDAO.isValidUser(username, password)) {
                showError(errorLabel, "Invalid username or password.");
                return;
            }

            User user = userDAO.createUser(username);
            initializeUserSession(user);

        } catch (SQLException e) {
            showError(errorLabel, "Database error! Please try again.");
            e.printStackTrace();
        }
    }

    private String validateInput(String username, String password) {
        if (username.isEmpty() && password.isEmpty()) return "Please enter both username and password.";
        if (username.isEmpty()) return "Please enter username.";
        if (password.isEmpty()) return "Please enter password.";
        if (password.length() < 8) return "Password must be at least 8 characters long.";
        return null;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setOpacity(1);
        errorLabel.setText(message);
    }

    private void initializeUserSession(User user) {
        GuestContext.setCurrentGuest(user);


        FeedService feedService = new FeedService(user);

        SceneManager.replaceFeed(feedService);

        HomePageController homePageController = (HomePageController) SceneManager.changeScene("home", "/src/view/fxml/Homepage.fxml", null);

        //homePageController.setFeedService(feedService);
        //homePageController.LoadUserPosts();
        homePageController.setLoginButtonVisibility(false);
        homePageController.setUserProfileAccessVisibility(true);

        SceneManager.closeSecondaryStage();
    }

}
