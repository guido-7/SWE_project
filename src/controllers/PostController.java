package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import src.businesslogic.PostService;
import src.domainmodel.Guest;
import src.domainmodel.Post;
import src.domainmodel.Role;
import src.domainmodel.User;
import src.servicemanager.FormattedTime;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;

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

    private PostService postService;
    private final FormattedTime formatter = new FormattedTime();
    private boolean isLiked = false;
    private boolean isDisliked = false;

    public PostController(PostService postService){
        this.postService = postService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        postButton.setOnAction(event -> goToPostPage());
        likeButton.setOnAction(event -> handleLikeButton());
        dislikeButton.setOnAction(event -> handleDislikeButton());
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
        checkUserVote();
    }

    private void checkUserVote() {
        Guest guest = GuestContext.getCurrentGuest();
        if (guest.getRole() == Role.GUEST) return;
        User currentUser = (User)guest;
        isLiked = postService.isLiked(currentUser.getId());
        isDisliked = postService.isDisliked(currentUser.getId());
        updateButtonStyles();
    }

    public void setData(Post post) throws SQLException {
        myVBox.setMaxHeight(180);
        setDataOnCard(post);
    }

    public void setDataPostPage(Post post) throws SQLException {
        myVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
        setDataOnCard(post);
    }

    private void handleLikeButton() {
        try {
            Guest guest = GuestContext.getCurrentGuest();
            postService.toggleLike(guest);
            updateScore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDislikeButton() {
        try {
            Guest guest = GuestContext.getCurrentGuest();
            postService.toggleDislike(guest);
            updateScore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateScore() throws SQLException {
        init_data();
        Post updatedPost = postService.getPost();
        scoreLabel.setText(updatedPost.getLikes() - updatedPost.getDislikes() + "");
    }

    private void updateButtonStyles() {
        // Rimuove tutte le classi esistenti
        likeButton.getStyleClass().removeAll("selected", "unselected");
        dislikeButton.getStyleClass().removeAll("selected", "unselected");

        if (isLiked) {
            likeButton.getStyleClass().add("selected");
            dislikeButton.getStyleClass().add("unselected");
        } else if (isDisliked) {
            dislikeButton.getStyleClass().add("selected");
            likeButton.getStyleClass().add("unselected");
        } else {
            likeButton.getStyleClass().add("unselected");
            dislikeButton.getStyleClass().add("unselected");
        }
    }

    private void goToPostPage(){
        String fxmlfile = "/src/view/fxml/PostPage.fxml";
        PostPageController postPageController = new PostPageController(postService);
        SceneManager.loadScene(fxmlfile, postPageController);
    }

    @Override
    public void init_data() {
        try {
            postService.refreshPost();
            setData(postService.getPost());
            checkUserVote();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}