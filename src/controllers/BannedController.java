package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.businesslogic.BannedService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class BannedController implements Initializable, Controller {
    @FXML
    private Label banDurationLabel;
    @FXML
    private Label banReasonLabel;
    @FXML
    private Button closeButton;

    private final BannedService bannedService;

    public BannedController(BannedService bannedService) {
        this.bannedService = bannedService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateLabels();
        closeButton.setOnMouseClicked(event -> {
            ((Stage) closeButton.getScene().getWindow()).close();
        });
    }

    private void updateLabels() {
        if (bannedService != null) {
            bannedService.setLabels(banDurationLabel, banReasonLabel);
        }
    }

    @Override
    public void init_data() throws SQLException {

    }

}
