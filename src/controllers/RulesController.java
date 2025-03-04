package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;
import src.domainmodel.Rule;
import src.orm.RulesDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class RulesController implements Controller {
    @FXML
    private Label community_title;
    @FXML
    private Label rule_title;
    @FXML
    private Label content;

    public void setRuleData(String communityID, String ruleTitle, String ruleContent) {
        community_title.setText(communityID);
        rule_title.setText(ruleTitle);
        content.setText(ruleContent);
    }

    @Override
    public void init_data() {

    }

}
