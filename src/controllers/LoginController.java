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
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink signUp;
    @FXML
    private Label errorLabel;

    private final LoginService loginService;

    public LoginController(LoginService loginService){
        this.loginService = loginService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(e -> handleLoginButtonClick());

        signUp.setOnAction(e -> {
            try {
                handleSignUpButtonAction();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void handleLoginButtonClick() {
        System.out.println("Login button clicked!");
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println("Username: " + username + ", Password: " + password);
        loginService.manageLogin(username, password, errorLabel);
    }

    private void handleSignUpButtonAction() throws IOException {
        SceneManager.changeSecondaryScene("signup", "/src/view/fxml/SignUp.fxml", new SignUpController());
    }

    @Override
    public void init_data() {
    }

}
