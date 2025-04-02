package src.controllers.componentcontrollers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import src.services.pageservices.CommunityService;
import src.controllers.pagecontrollers.AdminPageController;
import src.usersession.GuestContext;

public class SubInfoComponentController{
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

    public SubInfoComponentController(CommunityService communityService) {
        this.communityService = communityService;
    }

    public void setSubInfoData(String nickname, String spentTime) {
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

    public void setCommunityService(CommunityService communityService){
        this.communityService = communityService;
    }

    public Pair<AnchorPane,SubInfoComponentController> getAnchorSubInfoCtrlPair(){
        return new Pair<>(UserInfoContainer,this);
    }

    public int getComponentSubId(){
        return subId;
    }
}
