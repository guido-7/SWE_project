package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import src.businesslogic.SignUpService;
import src.servicemanager.SceneManager;
import java.io.IOException;

public class SignUpController implements Controller {

    @FXML
    private TextField name;

    @FXML
    private TextField surname;

    @FXML
    private TextField nickname;

    @FXML
    private PasswordField password_id;

    @FXML
    private Label not_name;

    @FXML
    private Label not_surname;

    @FXML
    private Label not_username;

    @FXML
    private Label not_password;

    private final SignUpService signUpService = new SignUpService();

    @FXML
    private void handleSignUpButtonAction() {
        System.out.println("Register link clicked!");

        String nicknameId = nickname.getText();
        String surnameId = surname.getText();
        String nameId = name.getText();
        String password = password_id.getText();

        resetErrorMessages();

        signUpService.registerUser(nicknameId, nameId, surnameId, password, not_username, not_name, not_surname, not_password);
    }

    @FXML
    private void back() {
        System.out.println("Back button clicked!");
        SceneManager.changeSecondaryScene("login", "/src/view/fxml/Login.fxml", new LoginController());
    }

    private void resetErrorMessages() {
        not_username.setOpacity(0);
        not_surname.setOpacity(0);
        not_name.setOpacity(0);
        not_password.setOpacity(0);
    }

    @FXML
    public static void openSignUpPage(Stage stage) {
        SceneManager.changeSecondaryScene("signup", "/src/view/fxml/SignUp.fxml", new SignUpController());
    }

    @Override
    public void init_data() {
    }
}
