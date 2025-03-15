package src.controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import src.FunctionalInterfaces.BackMessage;

import java.sql.SQLException;

public class ConfirmationDialogPageController implements Controller {

    private BackMessage callback;

    @FXML
    private Button yesButton;

    @FXML
    private Button noButton;

    @FXML
    private Label QuestionLabel;

    public void setCallback(BackMessage callback) {
        this.callback = callback;
    }

    @FXML
    private void onYesClicked() throws SQLException {
        if (callback != null) {
            callback.onResult(true);
        }
    }

    @FXML
    private void onNoClicked() throws SQLException {
        if (callback != null) {
            callback.onResult(false);
        }
    }
    public void setQuestion(String question) {
        QuestionLabel.setText(question);
    }
    @Override
    public void init_data() {

    }
}



