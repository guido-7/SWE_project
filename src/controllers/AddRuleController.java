package src.controllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AddRuleController implements Initializable,Controller {
    @FXML
    private SplitMenuButton PriorityMenu;

    @FXML
    private Button SaveRuleButton;

    @FXML
    private TextArea TextAreaNewRule;

    @FXML
    private TextField TitleRule;

    private CommunityService communityService;

    AddRuleController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        SaveRuleButton.setOnAction(event -> {
            String title = TitleRule.getText();
            String content = TextAreaNewRule.getText();
            int priority = Integer.parseInt(PriorityMenu.getText());
            communityService.addRule(title, content, priority);
            ((Stage) SaveRuleButton.getScene().getWindow()).close();
        });
    }

    @Override
    public void init_data() throws SQLException {

    }
}
