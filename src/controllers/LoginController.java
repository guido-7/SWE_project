package src.controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.businesslogic.LoginService;
import src.servicemanager.SceneManager;

public class LoginController implements Controller {


    @FXML
    private TextField user_id;

    @FXML
    private PasswordField password_id;

    @FXML
    private Label errorLabel;

    HomePageController homePageController;

    @FXML
    private void handleLoginButtonAction() {
        System.out.println("Login button clicked!");
        String username = user_id.getText();
        String password = password_id.getText();
        System.out.println("Username: " + username + ", Password: " + password);
        LoginService loginService = new LoginService();
        loginService.manageLogin(username, password,errorLabel,homePageController);

    }

    @FXML
    private void handleSignUpButtonAction() {
        Stage stage = (Stage) user_id.getScene().getWindow();
        SignUpController.openSignUpPage(stage);
    }

    public static void openLoginPage() {
        LoginController loginController = new LoginController();
        SceneManager.changeScene("login", "/src/view/fxml/Login.fxml", loginController);
    }

    public void setHomePageController(HomePageController homePageController) {
        this.homePageController = homePageController;
    }

    @Override
    public void init_data() {

    }
}
