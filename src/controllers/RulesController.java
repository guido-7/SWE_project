package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

import src.businesslogic.CommunityService;
import src.domainmodel.Role;
import src.servicemanager.GuestContext;
import java.sql.SQLException;

public class RulesController implements Controller {
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

    public void setRuleData(String communityID, String ruleTitle, String ruleContent) {
        rule_title.setText(ruleTitle);
        content.setText(ruleContent);
        if (GuestContext.getCurrentGuest().getRole() == Role.ADMIN) {
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

    private void handleDeleteRule() throws SQLException {
        CommunityController communityController = (CommunityController) GuestContext.getCurrentController();
        communityController.getPostsContainer().getChildren().remove(RuleContainer);
        communityService.deleteRule(ruleId);
    }

    @Override
    public void init_data() {
    }

}
