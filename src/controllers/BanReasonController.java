package src.controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import src.FunctionalInterfaces.BanUserCallback;
import src.businesslogic.CommunityService;

public class BanReasonController implements Initializable {
    @FXML
    private TextArea banReasonTextArea;
    @FXML
    private Button banButton;


    private final CommunityService communityService;
    private final String nickname;
    private final int reportedId;
    private BanUserCallback banUserCallback;

    public BanReasonController(int reportedId,String nickname,CommunityService communityService,BanUserCallback banUserCallBack){
        this.communityService = communityService;
        this.nickname = nickname;
        this.reportedId = reportedId;
        this.banUserCallback = banUserCallBack;
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        banButton.setText("Ban"+ nickname );
        banButton.setOnMouseClicked( event ->{
            String banReason = banReasonTextArea.getText();
            //validateBanReason(banReason);
            communityService.banUser(reportedId,banReason);
            banUserCallback.execute();
            ((Stage) banButton.getScene().getWindow()).close(); // Chiude la finestra


        });

    }

}
