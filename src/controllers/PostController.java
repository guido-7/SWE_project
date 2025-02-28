package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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
    @FXML
    private ImageView deletePostButton;
    @FXML
    private Button savePostButton;

    private PostService postService;
    private final FormattedTime formatter = new FormattedTime();
    private boolean isLiked = false;
    private boolean isDisliked = false;
    private boolean isSaved = false;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        postButton.setOnAction(event -> goToPostPage());
        likeButton.setOnAction(event -> handleLikeButton());
        dislikeButton.setOnAction(event -> handleDislikeButton());
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
        checkUserVote();
        checkSavedPost();
        checkPostVisibility();
    }

    private void checkPostVisibility() {
        Guest guest = GuestContext.getCurrentGuest();

        if (guest.getRole() != Role.GUEST) {
            User user = (User) guest;
            if (postService.isPostOwner(user.getId())) {
                deletePostButton.setVisible(true);
            } else {
                deletePostButton.setVisible(false);
            }
            savePostButton.setVisible(true);
        } else {
            deletePostButton.setVisible(false);
            savePostButton.setVisible(false);
        }
    }

    private void checkUserVote() {
        Guest guest = GuestContext.getCurrentGuest();
        if (guest.getRole() == Role.GUEST) {
            return;
        }
        User currentUser = (User) guest;
        isLiked = postService.isLiked(currentUser.getId());
        isDisliked = postService.isDisliked(currentUser.getId());
        updateButtonStyles();
    }

    private void checkSavedPost() throws SQLException {
        Guest guest = GuestContext.getCurrentGuest();
        if (guest.getRole() == Role.GUEST) {
            return;
        }
        User currentUser = (User) guest;
        isSaved = postService.isSaved(currentUser.getId());
        ImageView image;
        if (isSaved) {
            image = new ImageView("/src/view/images/SavedClickIcon.png");
        } else {
            image = new ImageView("/src/view/images/SavedIcon.png");
        }
        image.setFitHeight(20);
        image.setFitWidth(20);
        savePostButton.setGraphic(image);
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
            if(guest.getRole() == Role.GUEST)
                return;
            User user = (User) guest;
            postService.toggleLike(user);
            updateScore();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDislikeButton() {
        try {
            Guest guest = GuestContext.getCurrentGuest();
            if(guest.getRole() == Role.GUEST)
                return;
            User user = (User) guest;
            postService.toggleDislike(user);
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
            checkUserVote();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}