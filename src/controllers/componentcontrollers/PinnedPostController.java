package src.controllers.componentcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import src.services.pageservices.CommunityService;
import src.controllers.Controller;
import src.factory.PageControllerFactory;
import src.controllers.pagecontrollers.PostPageController;
import src.domainmodel.Post;
import src.usersession.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PinnedPostController implements Controller, Initializable {
    @FXML
    private Label postTitle;
    @FXML
    private ImageView openPostButton;

    int postId;
    CommunityService communityService;

    public PinnedPostController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setOnEvent();
    }

    @Override
    public void setOnEvent(){
        openPostButton.setOnMouseClicked(e -> openPostPage());
    }

    private void openPostPage() {
        try {
            Post post = communityService.getPost(postId);
            String fxmlfile = "/src/view/fxml/PostPage.fxml";
            PostPageController postPageController = PageControllerFactory.createPostPageController(post);
            SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
            SceneManager.loadScene(fxmlfile, postPageController);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPostTitle(String title) {
        postTitle.setText(title);
    }

    public void setPostId(int post_id) {
        postId = post_id;
    }
}
