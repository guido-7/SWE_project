package src.controllers.authenpagecontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import src.services.authenservices.LoginPageService;
import src.services.authenservices.SignUpPageService;
import src.controllers.PageController;
import src.usersession.SceneManager;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpPageController implements PageController, Initializable {
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
    @FXML
    private Button SignUpButton;
    @FXML
    private ImageView backButton;

    private final SignUpPageService signUpService = new SignUpPageService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SignUpButton.setOnAction(e -> handleSignUpButtonClick());
        backButton.setOnMouseClicked(e -> back());
    }

    @Override
    public void init_data() {

    }

    @Override
    public void setOnEvent() {

    }

    private void handleSignUpButtonClick() {
        System.out.println("Register button clicked!");

        String nicknameId = nickname.getText();
        String surnameId = surname.getText();
        String nameId = name.getText();
        String password = password_id.getText();

        resetErrorMessages();

        signUpService.registerUser(nicknameId, nameId, surnameId, password, not_username, not_name, not_surname, not_password);
    }

    private void back() {
        System.out.println("Back button clicked!");
        SceneManager.changeSecondaryScene("login", "/src/view/fxml/Login.fxml", new LoginPageController(new LoginPageService()));
    }

    private void resetErrorMessages() {
        not_username.setOpacity(0);
        not_surname.setOpacity(0);
        not_name.setOpacity(0);
        not_password.setOpacity(0);
    }

}
