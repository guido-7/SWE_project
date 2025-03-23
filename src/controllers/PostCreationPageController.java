package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import src.businesslogic.PostCreationService;
import src.businesslogic.SearchService;
import src.controllers.helpers.CommunitySearchHelper;
import src.domainmodel.Community;
import src.domainmodel.User;
import src.servicemanager.GuestContext;
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
    @FXML
    private Label errorLabel;

    int selectedCommunityId;
    private final PostCreationService postCreationService;
    private CommunitySearchHelper communitySearchHelper;

    public PostCreationPageController(PostCreationService postCreationService) {
        this.postCreationService = postCreationService;
    }

    @Override
    public void init_data() throws SQLException {
        communitySearchBar.clear();
        titleField.clear();
        contentArea.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        exitButton.setOnMouseClicked(e -> {
            try {
                SceneManager.loadPreviousScene();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        postButton.setOnMouseClicked(e -> {
            String title = titleField.getText();
            String content = contentArea.getText();
            String community = communitySearchBar.getText();
            postCreationService.setErrorLabel(errorLabel);
            int userId = ((User) GuestContext.getCurrentGuest()).getId();
            try {
                if(!(postCreationService.createPost(community, title, content, selectedCommunityId, userId))) return;
                SceneManager.loadPreviousScene(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        SearchService searchService = new SearchService();
        communitySearchHelper = new CommunitySearchHelper(communitySearchBar, searchService::searchSubscribedCommunities, this::selectCommunity);
        communitySearchHelper.setupSearchListener();
    }

    private void selectCommunity(Community community) {
        communitySearchBar.setText(community.getTitle());
        communitySearchBar.positionCaret(communitySearchBar.getText().length());
        communitySearchBar.setEditable(false);
        selectedCommunityId = community.getId();
    }

    // For tests
    public CommunitySearchHelper getCommunitySearchHelper(){
        return communitySearchHelper;
    }
}