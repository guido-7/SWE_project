package src.controllers.pagecontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import src.services.pageservices.PostCreationPageService;
import src.services.SearchService;
import src.controllers.PageController;
import src.utils.CommunitySearchHelper;
import src.domainmodel.Community;
import src.domainmodel.User;
import src.usersession.GuestContext;
import src.usersession.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PostCreationPageController implements PageController, Initializable {
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
    private final PostCreationPageService postCreationPageService;
    private CommunitySearchHelper communitySearchHelper;

    public PostCreationPageController(PostCreationPageService postCreationPageService) {
        this.postCreationPageService = postCreationPageService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setOnEvent();
        SearchService searchService = new SearchService();
        communitySearchHelper = new CommunitySearchHelper(communitySearchBar, searchService::searchSubscribedCommunities, this::selectCommunity);
        communitySearchHelper.setupSearchListener();
    }

    @Override
    public void init_data() throws SQLException {
        communitySearchBar.clear();
        titleField.clear();
        contentArea.clear();
    }

    @Override
    public void setOnEvent() {
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
            postCreationPageService.setErrorLabel(errorLabel);
            int userId = ((User) GuestContext.getCurrentGuest()).getId();
            try {
                if(!(postCreationPageService.createPost(community, title, content, selectedCommunityId, userId))) return;
                SceneManager.loadPreviousScene(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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