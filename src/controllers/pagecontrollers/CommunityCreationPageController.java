package src.controllers.pagecontrollers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import src.services.pageservices.CommunityCreationPageService;
import src.controllers.PageController;
import src.usersession.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;


public class CommunityCreationPageController implements PageController, Initializable {
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
    @FXML
    private ImageView exitButton;
    @FXML
    private Label errorLabel;

    private final CommunityCreationPageService communityCreationPageService;

    public CommunityCreationPageController(CommunityCreationPageService communityCreationPageService){
        this.communityCreationPageService = communityCreationPageService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init_data();
        setOnEvent();
    }

    @Override
    public void setOnEvent() {
        createButton.setOnMouseClicked(e -> createCommunity());
        exitButton.setOnMouseClicked(e -> {
            SceneManager.changeScene("home","/src/view/fxml/HomePage.fxml",null);
        });
    }

    @Override
    public void init_data() {
        clearAllFields();
    }

    private void clearAllFields() {
        descriptionArea.clear();
        rule1.clear();
        rule2.clear();
        rule3.clear();
        titleField.clear();
        RuleTitle1.clear();
        RuleTitle2.clear();
        RuleTitle3.clear();
    }

    private void createCommunity(){
        String title = titleField.getText();
        String description = descriptionArea.getText();
        communityCreationPageService.setErrorLabel(errorLabel);
        try {
            int communityId = communityCreationPageService.createCommunity(title, description);
            // Se communityId è -1 significa che c'è stato un errore, quindi non cambiare pagina
            if (communityId == -1) return;

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
            communityCreationPageService.saveRules(communityId,ruleMapping);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        SceneManager.changeScene("home", "/src/view/fxml/HomePage.fxml",null);

    }


}
