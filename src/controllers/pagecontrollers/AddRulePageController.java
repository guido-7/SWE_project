package src.controllers.pagecontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.services.CommunityService;
import src.controllers.PageController;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class AddRulePageController implements Initializable, PageController {
    @FXML
    private SplitMenuButton PriorityMenu;
    @FXML
    private Button SaveRuleButton;
    @FXML
    private TextArea TextAreaNewRule;
    @FXML
    private TextField TitleRule;

    private final CommunityService communityService;

    public AddRulePageController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init_data();
        setOnEvent();
    }

    @Override
    public void setOnEvent() {
        SaveRuleButton.setOnAction(event ->handleSaveRuleButtonClick());
    }

    @Override
    public void init_data(){
        PriorityMenu.getItems().clear();
        int lastPriority = communityService.getLastPriority();
        PriorityMenu.getItems().addAll(
                IntStream.rangeClosed(1, lastPriority)
                        .mapToObj(i -> {
                            MenuItem item = new MenuItem(String.valueOf(i));
                            item.setOnAction(event -> {
                                PriorityMenu.setText(item.getText());
                            });
                            return item;
                        })
                        .toList()
        );
    }

    private void handleSaveRuleButtonClick(){
        String title = TitleRule.getText();
        String content = TextAreaNewRule.getText();
        int priority = Integer.parseInt(PriorityMenu.getText());
        communityService.addRule(title, content, priority);
        ((Stage) SaveRuleButton.getScene().getWindow()).close();
    }
}
