package src.businesslogic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import src.domainmodel.User;
import src.orm.UserDAO;

import java.sql.SQLException;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;


    @FXML
    private void handleLoginButtonAction(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
        } else if (username.equals("admin") && password.equals("password")) {
            errorLabel.setText("Login successful!");
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }
}
