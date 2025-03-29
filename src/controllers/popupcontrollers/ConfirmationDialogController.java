package src.controllers.popupcontrollers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import src.controllers.BackMessage;
import src.controllers.Controller;

import java.sql.SQLException;

public class ConfirmationDialogController implements Controller {

    private BackMessage callback;

    @FXML
    private Button yesButton;

    @FXML
    private Button noButton;

    @FXML
    private Label QuestionLabel;

    @Override
    public void setOnEvent() {

    }

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
}



