package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import src.servicemanager.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PostCreationPageController implements Controller, Initializable {

    @FXML
    private TextField communitySearchBar;

    @FXML
    private TextArea contentArea;

    @FXML
    private ImageView exitButton;

    @FXML
    private Button postButton;

    @FXML
    private TextField titleField;

    PostCreationPageController() {

    }

    @Override
    public void init_data() throws SQLException {
        contentArea.clear();
        titleField.clear();
        communitySearchBar.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        exitButton.setOnMouseClicked(e -> {
            SceneManager.changeScene("home","/src/view/fxml/HomePage.fxml",null);
        });

    }
}
