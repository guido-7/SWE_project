package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.businesslogic.LoginService;
import src.servicemanager.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Controller, Initializable {

    @FXML
    private TextField user_id;

    @FXML
    private PasswordField password_id;

    @FXML
    private Label errorLabel;

    @FXML
    private Button login_button;

    @FXML
    private Hyperlink sign_up;

    private HomePageController homePageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        login_button.setOnAction(e -> handleLoginButtonAction());
        sign_up.setOnAction(e -> {
            try {
                handleSignUpButtonAction();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void handleLoginButtonAction() {
        System.out.println("Login button clicked!");
        String username = user_id.getText();
        String password = password_id.getText();
        System.out.println("Username: " + username + ", Password: " + password);
        LoginService loginService = new LoginService();
        loginService.manageLogin(username, password, errorLabel, homePageController);
    }

    private void handleSignUpButtonAction() throws IOException {
        Stage stage = (Stage) user_id.getScene().getWindow();
        SignUpController.openSignUpPage(stage);
    }

    public void setHomePageController(HomePageController homePageController) {
        this.homePageController = homePageController;
    }

    @Override
    public void init_data() {

    }

}
