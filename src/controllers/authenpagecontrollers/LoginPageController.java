package src.controllers.authenpagecontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import src.services.authenservices.LoginPageService;
import src.controllers.PageController;
import src.usersession.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginPageController implements PageController, Initializable {
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

    private final LoginPageService loginPageService;

    public LoginPageController(LoginPageService loginPageService){
        this.loginPageService = loginPageService;
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

    @Override
    public void init_data() {
    }

    @Override
    public void setOnEvent() {

    }

    private void handleLoginButtonClick() {
        System.out.println("Login button clicked!");
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println("Username: " + username + ", Password: " + password);
        loginPageService.manageLogin(username, password, errorLabel);
    }

    private void handleSignUpButtonAction() throws IOException {
        SceneManager.changeSecondaryScene("signup", "/src/view/fxml/SignUp.fxml", new SignUpPageController());
    }

}
