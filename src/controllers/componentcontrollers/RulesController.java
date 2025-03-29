package src.controllers.componentcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

import src.services.CommunityService;
import src.controllers.pagecontrollers.CommunityPageController;
import src.domainmodel.Role;
import src.usersession.GuestContext;
import java.sql.SQLException;

public class RulesController{
    @FXML
    private Label rule_title;
    @FXML
    private Label content;
    @FXML
    private VBox deleteRuleIconContainer;
    @FXML
    private VBox RuleContainer;

    private final CommunityService communityService;
    private final int ruleId;

    public RulesController(CommunityService communityService, int ruleId) {
        this.communityService = communityService;
        this.ruleId = ruleId;
    }

    public void setRuleData(String ruleTitle, String ruleContent) {
        rule_title.setText(ruleTitle);
        content.setText(ruleContent);
        if (GuestContext.getCurrentGuest().getRole() == Role.ADMIN)
            handleAdminIsWatching();
    }
    private void handleDeleteRule() throws SQLException {
        CommunityPageController communityPageController = (CommunityPageController) GuestContext.getCurrentController();
        communityPageController.getPostsContainer().getChildren().remove(RuleContainer);
        communityService.deleteRule(ruleId);
    }

    private void handleAdminIsWatching(){
        ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/src/view/images/delete.png")));
        deleteIcon.setFitWidth(25);
        deleteIcon.setFitHeight(25);
        deleteIcon.setOnMouseClicked(e -> {
            try {
                handleDeleteRule();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteRuleIconContainer.getChildren().add(deleteIcon);
    }

}

