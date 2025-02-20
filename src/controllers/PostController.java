package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import src.businesslogic.PostService;
import src.domainmodel.Post;
import src.servicemanager.FormattedTime;
import src.servicemanager.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class PostController implements Controller, Initializable {
    @FXML
    private Label community;
    @FXML
    private Label username;
    @FXML
    private Label content;
    @FXML
    private Label date;
    @FXML
    private Label title;
    @FXML
    private Label scoreLabel;
    @FXML
    private VBox myVBox;
    @FXML
    private Button postButton;

    private PostService postService;
    private final FormattedTime formatter = new FormattedTime();

    public PostController(PostService postService){
        this.postService = postService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        postButton.setOnAction(event -> goToPostPage());
    }

    private void setDataOnCard(Post post) throws SQLException {
        String communityTitle = postService.getCommunityTitle();
        community.setText("r/" + communityTitle);

        String nickname = postService.getnickname();
        username.setText(nickname);

        date.setText(formatter.getFormattedTime(post.getTime()));
        title.setText(post.getTitle());
        content.setText(post.getContent());
        scoreLabel.setText(post.getLikes() - post.getDislikes() + "");
    }

    public void setData(Post post) throws SQLException {
        myVBox.setMaxHeight(180);
        setDataOnCard(post);
    }

    public void setDataPostPage(Post post) throws SQLException {
        myVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
        setDataOnCard(post);
    }

    public Post getPost() throws SQLException {
        return postService.getPost();
    }

    private void  goToPostPage(){
        String fxmlfile = "/src/view/fxml/PostPage.fxml";
        PostPageController postPageController = new PostPageController(postService);
        SceneManager.loadScene(fxmlfile, postPageController);
    }

    @Override
    public void init_data() {

    }
}
