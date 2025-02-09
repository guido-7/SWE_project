package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.orm.UserDAO;

import java.io.IOException;

import static src.controllers.LoginController.openLoginPage;

public class SignUpController {

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

        String userId = nickname.getText();
        String surnameId = surname.getText();
        String nameId = name.getText();
        String password = password_id.getText();

        boolean isValid = true; // Flag per controllare se possiamo procedere

        // Reset dei messaggi di errore
        not_username.setOpacity(0);
        not_surname.setOpacity(0);
        not_name.setOpacity(0);
        not_password.setOpacity(0);

        // Controllo se i campi sono vuoti e mostro il messaggio di errore
        if (userId.isEmpty()) {
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
        }

        // Se c'è almeno un errore, esco dalla funzione
        if (!isValid) {
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            if (userDAO.isRegisteredUser(userId)) {
                not_username.setOpacity(1);
                not_username.setText("User already registered");
                return; // Blocca la registrazione se l'utente esiste già
            }

            // Prova a registrare l'utente
            try {
                userDAO.registerUser(userId, surnameId, nameId, password);
            } catch (Exception e) {
                System.err.println("Registration failed: " + e.getMessage());
                return;
            }
            System.out.println("User registered successfully!");

            Stage stage = (Stage) name.getScene().getWindow();
            LoginController.openLoginPage(stage);

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    public static void openSignUpPage(Stage stage) {
        try {
            System.out.println("Opening Sign Up Page...");
            FXMLLoader signUpPage = new FXMLLoader(SignUpController.class.getResource("/src/view/fxml/SignUp.fxml"));
            stage.setScene(new Scene(signUpPage.load()));
            stage.setTitle("Sign Up Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Sign Up Page.");
        }
    }



}