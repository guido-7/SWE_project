package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import src.businesslogic.CommunityService;
import src.businesslogic.PostService;
import src.domainmodel.Post;
import src.servicemanager.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PinnedPostController implements Controller, Initializable {

    CommunityService communityService;
    Integer postId;

    @FXML
    private Label postTitle;
    @FXML
    private ImageView openPostButton;

    public PinnedPostController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        openPostButton.setOnMouseClicked(e -> {openPost();});
    }

    private void openPost() {
        try {
            Post post = communityService.getPost(postId);
            String fxmlfile = "/src/view/fxml/PostPage.fxml";
            PostPageController postPageController = new PostPageController(new PostService(post));
            SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
            SceneManager.loadScene(fxmlfile, postPageController);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPostTitle(String title) {
        postTitle.setText(title);
    }

    @Override
    public void init_data() throws SQLException {

    }

    public void setPostId(Integer post_id) {
        postId = post_id;
    }
}
