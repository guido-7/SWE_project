package src.businesslogic;

import javafx.scene.control.Label;
import src.orm.UserDAO;
import src.servicemanager.SceneManager;
import src.controllers.LoginController;

import java.util.Map;

public class SignUpService {

    private final UserDAO userDAO = new UserDAO();

    public void registerUser(String nickname, String name, String surname, String password,
                             Label notUsername, Label notName, Label notSurname, Label notPassword) {

        StringBuilder validationMessage = validateInput(nickname, name, surname, password);

        if (!validationMessage.isEmpty()) {
            showError(nickname, name, surname, password, notUsername, notName, notSurname, notPassword);
            return;
        }

        try {
            if (userDAO.isRegisteredUser(nickname)) {
                notUsername.setOpacity(1);
                notUsername.setText("User already registered");
                return;
            }

            userDAO.save(Map.of("nickname", nickname, "name", name, "surname", surname));
            int id = userDAO.getUserId(nickname);
            userDAO.registerUserAccessInfo(id, nickname, password);

            System.out.println("User registered successfully!");
            SceneManager.changeSecondaryScene("login", "/src/view/fxml/Login.fxml", new LoginController());

        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }

    private StringBuilder validateInput(String nickname, String name, String surname, String password) {
        StringBuilder errorMessages = new StringBuilder();

        if (nickname.isEmpty()) {
            errorMessages.append("Username is required.\n");
        }
        if (name.isEmpty()) {
            errorMessages.append("Name is required.\n");
        }
        if (surname.isEmpty()) {
            errorMessages.append("Surname is required.\n");
        }
        if (password.isEmpty()) {
            errorMessages.append("Password is required.\n");
        } else if (password.length() < 8) {
            errorMessages.append("Password must be at least 8 characters long.\n");
        }

        return errorMessages;
    }

    private void showError(String nickname, String name, String surname, String password,
                           Label notUsername, Label notName, Label notSurname, Label notPassword) {

        resetErrors(notUsername, notName, notSurname, notPassword);

        if (nickname.isEmpty() && notUsername != null) {
            notUsername.setOpacity(1);
            notUsername.setText("Username is required.");
        }
        if (name.isEmpty() && notName != null) {
            notName.setOpacity(1);
            notName.setText("Name is required.");
        }
        if (surname.isEmpty() && notSurname != null) {
            notSurname.setOpacity(1);
            notSurname.setText("Surname is required.");
        }
        if (password.isEmpty() && notPassword != null) {
            notPassword.setOpacity(1);
            notPassword.setText("Password is required.");
        } else if (password.length() < 8 && notPassword != null) {
            notPassword.setOpacity(1);
            notPassword.setText("Password must be at least 8 characters long.");
        }
    }

    private void resetErrors(Label notUsername, Label notName, Label notSurname, Label notPassword) {
        if (notUsername != null) {
            notUsername.setOpacity(0);
            notUsername.setText("");
        }
        if (notName != null) {
            notName.setOpacity(0);
            notName.setText("");
        }
        if (notSurname != null) {
            notSurname.setOpacity(0);
            notSurname.setText("");
        }
        if (notPassword != null) {
            notPassword.setOpacity(0);
            notPassword.setText("");
        }
    }
}
