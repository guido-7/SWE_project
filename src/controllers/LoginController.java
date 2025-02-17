package src.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.businesslogic.FeedService;
import src.domainmodel.User;
import src.orm.UserDAO;
import src.servicemanager.SceneManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    private HomePageController homePageController;

    @FXML
    private TextField user_id;

    @FXML
    private PasswordField password_id;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLoginButtonAction() {
        System.out.println("Login button clicked!");
        UserDAO userDAO = new UserDAO();
        String username = user_id.getText();
        String password = password_id.getText();
        System.out.println("Username: " + username + ", Password: " + password);

        if (username.isEmpty() && password.isEmpty()) {
            errorLabel.setOpacity(1);
            errorLabel.setText("Please enter both username and password.");
        } else if(username.isEmpty()) {
            errorLabel.setOpacity(1);
            errorLabel.setText("Please enter username.");
        }else if (password.isEmpty()) {
            errorLabel.setOpacity(1);
            errorLabel.setText("Please enter password.");
        } else if (password.length() < 8) {
            errorLabel.setOpacity(1);
            errorLabel.setText("Password must be at least 8 characters long.");
        } else{
            try {
                if (userDAO.isValidUser(username, password)) {
                    User user = userDAO.createUser(username);
                    //Stage stage = (Stage) user_id.getScene().getWindow();

                    FeedService feedService = new FeedService(user);
                    homePageController.setFeedService(feedService);
                    SceneManager.changeScene("home","/src/view/fxml/Homepage.fxml",homePageController);
                    homePageController.LoadUserPosts();
                    homePageController.setLoginButtonVisibility(false);
                    homePageController.setUserProfileAccessVisibility(true);
                    SceneManager.closeSecondaryStage();
                    //SceneManager.show();
                    //HomePageController.openHomePage(user,stage);
                } else {
                    errorLabel.setOpacity(1);
                    errorLabel.setText("Invalid username or password.");
                }
            } catch (SQLException e) {
                errorLabel.setOpacity(1);
                errorLabel.setText("Database error!");
                e.printStackTrace();
            }
        }
    }

//    public static void openLoginPage(Stage stage) {
//        try {
//            System.out.println("Opening Login Page...");
//            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/src/view/fxml/Login.fxml"));
//            stage.setScene(new Scene(loader.load()));
//            stage.setTitle("Login Page");
//            stage.show();
//        } catch (IOException e) {
//            System.err.println("Error loading Login Page: " + e.getMessage());
//        }
//    }

    @FXML
    private void handleSignUpButtonAction() {
        Stage stage = (Stage) user_id.getScene().getWindow();
        SignUpController.openSignUpPage(stage);
    }

    public void setHomePageController(HomePageController homePageController) {
        this.homePageController = homePageController;
    }

    public static void openLoginPage() {
        LoginController loginController = new LoginController();
        SceneManager.changeScene("login", "/src/view/fxml/Login.fxml", loginController);
    }



}
