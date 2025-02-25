package src.controllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import src.businesslogic.CommunityCreationService;
import src.servicemanager.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;


public class CommunityCreationPageController implements Controller, Initializable {
    @FXML
    private Button createButton;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Text headerTitle;

    @FXML
    private TextArea rule1;

    @FXML
    private TextArea rule2;

    @FXML
    private TextArea rule3;

    @FXML
    private VBox rulesContainer;

    @FXML
    private TextField titleField;

    @FXML
    private TextField RuleTitle1;

    @FXML
    private TextField RuleTitle2;

    @FXML
    private TextField RuleTitle3;

    private final CommunityCreationService communityCreationService;

    public CommunityCreationPageController(CommunityCreationService communityCreationService){
        this.communityCreationService = communityCreationService;
    }



    @Override
    public void init_data() throws SQLException {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        createButton.setOnMouseClicked(e -> {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            try {
                int communityId = communityCreationService.createCommunity(title, description);
                ArrayList<TextArea> rules = new ArrayList<>(List.of(rule1, rule2, rule3));
                ArrayList<TextField> rulesTitle = new ArrayList<>(List.of(RuleTitle1, RuleTitle2, RuleTitle3));
                Map<Integer,ArrayList<String>> ruleMapping = new HashMap<>();
                for (int i = 0 ; i < rulesTitle.size();i++) {
                    ArrayList<String> mergedContent = new ArrayList<String>();
                    String titleOfRule = rulesTitle.get(i).getText();
                    String contentOfRule = rules.get(i).getText();
                    mergedContent.add(titleOfRule);
                    mergedContent.add(contentOfRule);
                    ruleMapping.put(i,mergedContent);
                }
                communityCreationService.saveRules(communityId,ruleMapping);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            SceneManager.changeScene("home", "/src/view/fxml/HomePage.fxml",null);
        });
    }
}
