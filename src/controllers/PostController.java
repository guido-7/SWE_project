package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import src.businesslogic.PostService;
import src.domainmodel.Guest;
import src.domainmodel.Post;
import src.domainmodel.Role;
import src.domainmodel.User;
import src.servicemanager.FormattedTime;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;
import src.servicemanager.VoteManager;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.SQLException;
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
    @FXML
    private Button likeButton;
    @FXML
    private Button dislikeButton;
    @FXML
    private ImageView deletePostButton;
    @FXML
    private Button savePostButton;
    @FXML
    private ImageView reportPostButton;
    @FXML
    private HBox HboxContainer;
    @FXML
    private VBox SignalTextContainer;

    private PostService postService;
    private final FormattedTime formatter = new FormattedTime();
    private VoteManager voteManager;
    private boolean isSaved = false;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reportPostButton.setVisible(false);
        reportPostButton.setManaged(false);

        reportPostButton.setOnMouseClicked(event->{
            postService.signalPost();
            HboxContainer.getChildren().remove(reportPostButton);
            SignalTextContainer.getChildren().add(new Text("Post has been reported"));
        });

        postButton.setOnAction(event -> goToPostPage());

        deletePostButton.setOnMouseClicked(event -> {
            try {
                handleDeletePost();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        savePostButton.setOnMouseClicked(event -> {
            try {
                handleSavePost();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        voteManager = new VoteManager(scoreLabel, likeButton, dislikeButton, postService);
    }

    private void handleDeletePost() throws SQLException {
        postService.deletePost(postService.getPost().getId());
        myVBox.getChildren().clear();
        Controller currentPageController = GuestContext.getCurrentController();
        if (currentPageController instanceof HomePageController homePageController) {
            homePageController.getPostsContainer().getChildren().remove(myVBox);
        } else if (currentPageController instanceof CommunityController communityPageController) {
            communityPageController.getPostsContainer().getChildren().remove(myVBox);
        } else if (currentPageController instanceof UserProfilePageController userProfilePageController) {
            userProfilePageController.getPostsContainer().getChildren().remove(myVBox);
        }
        SceneManager.getPrimaryStage().show();
    }

    private void handleSavePost() throws SQLException {
        User user = (User) GuestContext.getCurrentGuest();
        ImageView image;
        if (!isSaved) {
            postService.addSavePost(user.getId(), postService.getPost().getId());
            image = new ImageView("/src/view/images/SavedClickIcon.png");
            isSaved = true;
        } else {
            postService.removeSavePost(user.getId(), postService.getPost().getId());
            image = new ImageView("/src/view/images/SavedIcon.png");
            isSaved = false;
        }
        image.setFitHeight(20);
        image.setFitWidth(20);
        savePostButton.setGraphic(image);
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
        checkPostVisibility();
    }

    private void checkPostVisibility() {
        Guest guest = GuestContext.getCurrentGuest();

        if (guest.getRole() != Role.GUEST) {
            User user = (User) guest;
            reportPostButton.setVisible(!postService.isAlreadyReported());
            reportPostButton.setManaged(!postService.isAlreadyReported());
            if (postService.isPostOwner(user.getId())) {
                reportPostButton.setVisible(false);
                reportPostButton.setManaged(false);
                deletePostButton.setVisible(true);
                deletePostButton.setManaged(true);
            } else {
                deletePostButton.setVisible(false);
                deletePostButton.setManaged(false);
            }
            savePostButton.setVisible(true);
            savePostButton.setManaged(true);
        } else {
            deletePostButton.setVisible(false);
            deletePostButton.setManaged(false);
            savePostButton.setVisible(false);
            savePostButton.setManaged(false);
        }
    }

    public void setData(Post post) throws SQLException {
        myVBox.setMaxHeight(180);
        setDataOnCard(post);
    }

    public void setDataPostPage(Post post) throws SQLException {
        myVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
        setDataOnCard(post);
    }

    private void goToPostPage() {
        String fxmlfile = "/src/view/fxml/PostPage.fxml";
        PostPageController postPageController = new PostPageController(postService);
        SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
        SceneManager.loadScene(fxmlfile, postPageController);
    }

    @Override
    public void init_data() {
        try {
            postService.refreshPost();
            setData(postService.getPost());
            voteManager.checkUserVote();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}