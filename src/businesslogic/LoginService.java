package src.businesslogic;

import javafx.scene.control.Label;
import src.controllers.HomePageController;
import src.domainmodel.User;
import src.orm.UserDAO;
import src.servicemanager.SceneManager;
import src.servicemanager.UserContext;

import java.sql.SQLException;

public class LoginService {

    UserDAO userDAO = new UserDAO();

    public void manageLogin(String username, String password, Label errorLabel,HomePageController homePageController) {
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
            initializeUserSession(user,homePageController);

        } catch (SQLException e) {
            showError(errorLabel, "Database error! Please try again.");
            e.printStackTrace();
        }
    }

    /**
     * Validates username and password.
     * @return error message if invalid, otherwise null.
     */
    private String validateInput(String username, String password) {
        if (username.isEmpty() && password.isEmpty()) return "Please enter both username and password.";
        if (username.isEmpty()) return "Please enter username.";
        if (password.isEmpty()) return "Please enter password.";
        if (password.length() < 8) return "Password must be at least 8 characters long.";
        return null;
    }

    /**
     * Displays an error message.
     */
    private void showError(Label errorLabel, String message) {
        errorLabel.setOpacity(1);
        errorLabel.setText(message);
    }

    /**
     * Initializes user session and navigates to the home page.
     */
    private void initializeUserSession(User user,HomePageController homePageController) {
        UserContext.setCurrentUser(user);

        FeedService feedService = new FeedService(user);
        homePageController.setFeedService(feedService);

        SceneManager.changeScene("home", "/src/view/fxml/Homepage.fxml", homePageController);

        homePageController.LoadUserPosts();
        homePageController.setLoginButtonVisibility(false);
        homePageController.setUserProfileAccessVisibility(true);

        SceneManager.closeSecondaryStage();
    }

}
