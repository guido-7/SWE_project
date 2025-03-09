package src.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPageController implements Initializable, Controller {
    @FXML
    private Pane BCPane;
    @FXML
    private Pane BLPane;
    @FXML
    private Pane BleftPane;
    @FXML
    private Pane CCPane;
    @FXML
    private Pane CLPane;
    @FXML
    private Pane CRPane;
    @FXML
    private Button DismissButton;
    @FXML
    private Button PromoteButton;
    @FXML
    private TextField SearchSubsBar;
    @FXML
    private Pane TCPane;
    @FXML
    private Pane TLPane;
    @FXML
    private Pane TRPane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PromoteButton.setVisible(false);
        DismissButton.setVisible(false);
        ArrayList<Pane> PaneGrid = new ArrayList<>(List.of(BCPane, BLPane, BleftPane, CCPane, CLPane, CRPane, TCPane, TLPane, TRPane));
        for (Pane pane : PaneGrid) {
            try {
                Pane UserInfoComponent = loadUserInfoComponent();
                pane.getChildren().add(UserInfoComponent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pane.setOnMouseClicked(event -> {
                if (!PromoteButton.isVisible() && !DismissButton.isVisible()) {
                    PromoteButton.setVisible(true);
                    DismissButton.setVisible(true);
                }
            });
        }
        PromoteButton.setOnMouseClicked(event->{
            //communityService.promote(subscriberId);
        });

        DismissButton.setOnMouseClicked(event->{
            //communityService.dismiss(subscriberId);
        });
    }

    private Pane loadUserInfoComponent() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/UserInfoComponent.fxml"));
        UserInfoComponentController UserInfoController = new UserInfoComponentController();
        loader.setController(UserInfoController);
        return loader.load();

    }

    @Override
    public void init_data() throws SQLException {

    }
}
