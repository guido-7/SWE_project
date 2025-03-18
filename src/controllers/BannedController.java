package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.businesslogic.BannedService;
import src.domainmodel.User;
import src.orm.UserDAO;
import src.servicemanager.FormattedTime;
import src.servicemanager.GuestContext;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
        closeButton.setOnMouseClicked(event ->handleCloseButtonClick());
    }

    private void updateLabels() {
        if (bannedService != null) {
            setLabels();
        }
    }

    @Override
    public void init_data() throws SQLException {

    }
    private void handleCloseButtonClick(){
        ((Stage) closeButton.getScene().getWindow()).close();
    }
    private void setLabels() {
        Map<String, String> bannedInfo = bannedService.getBannedInfo();
        if (bannedInfo != null) {
            LocalDateTime banDate = LocalDateTime.parse(bannedInfo.get("ban_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            FormattedTime formattedTime = new FormattedTime();
            String formattedBanDuration = formattedTime.getBanTime(banDate);

            banDurationLabel.setText(formattedBanDuration);
            banReasonLabel.setText(bannedInfo.get("reason"));
        } else {
            banDurationLabel.setText("N/A");
            banReasonLabel.setText("N/A");
        }

    }

}
