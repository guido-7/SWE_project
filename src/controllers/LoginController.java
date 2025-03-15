package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.businesslogic.LoginService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Controller, Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signUp;
    @FXML
    private Label errorLabel;

    private HomePageController homePageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(e -> handleLoginButtonAction());

        signUp.setOnAction(e -> {
            try {
                handleSignUpButtonAction();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void handleLoginButtonAction() {
        System.out.println("Login button clicked!");
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println("Username: " + username + ", Password: " + password);
        LoginService loginService = new LoginService();
        loginService.manageLogin(username, password, errorLabel, homePageController);
    }

    private void handleSignUpButtonAction() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        SignUpController.openSignUpPage(stage);
    }

    public void setHomePageController(HomePageController homePageController) {
        this.homePageController = homePageController;
    }

    @Override
    public void init_data() {
    }

}
