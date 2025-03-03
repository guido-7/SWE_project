package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.FunctionalInterfaces.BanUserCallback;
import src.businesslogic.CommunityService;
import src.domainmodel.PostWarnings;
import src.servicemanager.GuestContext;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ModeratorDecisionController implements Controller, Initializable {
    @FXML
    private Pane pane;
    @FXML
    private TextField dayfield;

    @FXML
    private TextField hourfield;

    @FXML
    private TextField monthfield;

    @FXML
    private Button BanButton;

    @FXML
    private Button IgnoreButton;

    @FXML
    private SplitMenuButton TimeMenuButton;

    @FXML
    private Button TimeOutButton;

    @FXML
    private Text UserText;

    @FXML
    private Text warningsText;

    @FXML
    private Text ReportNoText;

    private final ArrayList<PostWarnings> warning;
    private  final CommunityService communityService;
    private final int reportedId;
    private final String reportedNickname;

    ModeratorDecisionController (ArrayList<PostWarnings> postWarnings, CommunityService communityService) {
        this.warning = postWarnings;
        this.communityService = communityService;
        reportedId = warning.getFirst().getReportedId();
        reportedNickname = warning.getFirst().getReported_nickname();
    }

    @Override
    public void init_data() throws SQLException {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        warningsText.setVisible(false);
        UserText.setText(reportedNickname);

        BanButton.setOnMouseClicked(event -> {
            try {
                BanUserCallback banUserCallBack = this::removeDecidedReport;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/BanReason.fxml"));
                BanReasonController banReasonController = new BanReasonController(reportedId, reportedNickname, communityService, banUserCallBack);
                loader.setController(banReasonController);
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //communityService.banUser(warning.getReportedId());
                //removeDecidedReport();
        });
        IgnoreButton.setOnMouseClicked(event -> ignoreReport());

        TimeOutButton.setOnMouseClicked(event -> {
            warningsText.setVisible(false);
            LocalDateTime time = getTime();
            if (time != null) {
                communityService.timeOutUser(reportedId, time);
                removeDecidedReport();
            }
        });



    }

    private void ignoreReport() {
        removeDecidedReport();
    }
    private LocalDateTime getTime() {
        try {
            int days = dayfield.getText().isEmpty() ? 0 : Integer.parseInt(dayfield.getText());
            int months = monthfield.getText().isEmpty() ? 0 : Integer.parseInt(monthfield.getText());
            int hours = hourfield.getText().isEmpty() ? 0 : Integer.parseInt(hourfield.getText());
            if (days == 0 && months == 0 && hours == 0) {
                warningsText.setVisible(true);
                warningsText.setText("Insert a time.");
                return null;
            }
            return LocalDateTime.now().plusMonths(months).plusDays(days).plusHours(hours);

        } catch (NumberFormatException e) {
            warningsText.setVisible(true);
            warningsText.setText("Please insert numerical values.");
            return null;
        }
    }
    public void removeDecidedReport() {
        CommunitySettingsController ctrl = (CommunitySettingsController)GuestContext.getCurrentController();
        ctrl.removeReport(pane);
        ctrl.removeReportsFromTable(warning);
    }
    public void setNumber(int number) {
        ReportNoText.setText(String.valueOf(number));
    }
}
