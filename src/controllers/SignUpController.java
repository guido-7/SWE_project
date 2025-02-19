package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.orm.UserDAO;
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

    @FXML
    private void handleSignUpButtonAction() {
        System.out.println("Register link clicked!");

        String nicknameId = nickname.getText();
        String surnameId = surname.getText();
        String nameId = name.getText();
        String password = password_id.getText();

        boolean isValid = true;

        // Reset dei messaggi di errore
        not_username.setOpacity(0);
        not_surname.setOpacity(0);
        not_name.setOpacity(0);
        not_password.setOpacity(0);

        if (nicknameId.isEmpty()) {
            not_username.setOpacity(1);
            not_username.setText("Missing name!");
            isValid = false;
        }
        if (surnameId.isEmpty()) {
            not_surname.setOpacity(1);
            not_surname.setText("Missing username!");
            isValid = false;
        }
        if (nameId.isEmpty()) {
            not_name.setOpacity(1);
            not_name.setText("Missing surname!");
            isValid = false;
        }
        if (password.isEmpty()) {
            not_password.setOpacity(1);
            not_password.setText("Missing password!");
            isValid = false;
        }else if(password.length() < 8){
            not_password.setOpacity(1);
            not_password.setText("Password must be at least 8 characters long.");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            if (userDAO.isRegisteredUser(nicknameId)) {
                not_username.setOpacity(1);
                not_username.setText("User already registered");
                return;
            } else {
                userDAO.registerUser(nicknameId, nameId, surnameId);
                int id = userDAO.getUserId(nicknameId);
                userDAO.registerUserAccess(id, nicknameId, password);
            }

            System.out.println("User registered successfully!");

            SceneManager.changeSecondaryScene("login", "/src/view/fxml/Login.fxml", new LoginController());

        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void back() {
        System.out.println("Back button clicked!");

        SceneManager.changeSecondaryScene("login", "/src/view/fxml/Login.fxml", new LoginController());
    }

    @FXML
    public static void openSignUpPage(Stage stage) {
        try {
            System.out.println("Opening Sign Up Page...");
            FXMLLoader signUpPage = new FXMLLoader(SignUpController.class.getResource("/src/view/fxml/SignUp.fxml"));
            stage.setScene(new Scene(signUpPage.load()));
            stage.setTitle("Sign Up Page");
            stage.show();
            SceneManager.setSecondaryStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Sign Up Page.");
        }
    }


    @Override
    public void init_data() {

    }
}