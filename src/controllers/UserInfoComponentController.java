package src.controllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import src.businesslogic.CommunityService;
import src.servicemanager.GuestContext;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserInfoComponentController implements Controller, Initializable {
    @FXML
    private Text SpentTime;
    @FXML
    private ImageView UserIcon;
    @FXML
    private Text UserNickname;
    @FXML
    private AnchorPane UserInfoContainer;

    private int subId;
    private CommunityService communityService;

    public UserInfoComponentController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            init_data();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init_data() throws SQLException {

    }

    public void setData(String nickname, String spentTime) {
        UserNickname.setText(nickname);
        SpentTime.setText(spentTime);
        setUI();
    }

    private void setUI() {
        UserInfoContainer.setOnMouseClicked(e -> {
            boolean isModerator = communityService.isModerator(subId);
            AdminPageController adminPageController = (AdminPageController) GuestContext.getCurrentController();
            if(isModerator){
                adminPageController.showDismissButton();
                adminPageController.setDismissText(UserNickname.getText());
            }
            else{
                adminPageController.showPromoteButton();
                adminPageController.setPromoteText(UserNickname.getText());
            }
            adminPageController.setDeletingPair(getAnchorSubInfoCtrlPair());
            adminPageController.setSubscribedId(subId);
        });
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }

    public void  setCommunityService(CommunityService communityService){
        this.communityService = communityService;
    }

    public Pair<AnchorPane,UserInfoComponentController> getAnchorSubInfoCtrlPair(){
        return new Pair<>(UserInfoContainer,this);
    }

    public int getComponentSubId(){
        return subId;
    }

}
