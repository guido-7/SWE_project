package src.controllers.componentcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import src.services.componentservices.PostService;
import src.controllers.Controller;
import src.controllers.pagecontrollers.CommunityPageController;
import src.controllers.pagecontrollers.HomePageController;
import src.controllers.pagecontrollers.UserProfilePageController;
import src.factory.ComponentFactory;
import src.controllers.pagecontrollers.PostPageController;
import src.domainmodel.Guest;
import src.domainmodel.Post;
import src.domainmodel.Role;
import src.domainmodel.User;
import src.usersession.SceneManager;
import src.utils.FormattedTime;
import src.usersession.GuestContext;
import src.utils.VoteManager;
import javafx.scene.layout.HBox;

import java.io.IOException;
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
    @FXML
    private Button pinPostButton;
    @FXML
    private VBox repliesContainer;

    private final PostService postService;
    private PostPageController postPageController;
    private final FormattedTime formatter = new FormattedTime();
    private VoteManager voteManager;
    private boolean isSaved = false;
    private boolean isOpenInPostPage = false;
    private boolean isReplying = false;
    private boolean isPinned = false;
    private Post post;

    public PostController(PostService postService) {
        this.postService = postService;
        this.post = postService.getPost();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reportPostButton.setVisible(false);
        reportPostButton.setManaged(false);
        voteManager = new VoteManager(scoreLabel, likeButton, dislikeButton, postService);
        setOnEvent();
    }

    @Override
    public void setOnEvent(){

        reportPostButton.setOnMouseClicked(event->{
            postService.signalPost();
            HboxContainer.getChildren().remove(reportPostButton);
            SignalTextContainer.getChildren().add(new Text("Post has been reported"));
        });

        postButton.setOnAction(event -> {
            if (isOpenInPostPage && !isReplying) {
                loadReplyBox();
            } else if (isOpenInPostPage && isReplying) {
                removeReplyField();
            } else {
                goToPostPage();
            }
        });

        deletePostButton.setOnMouseClicked(event -> {
            try {
                handleDeletePost();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        pinPostButton.setOnMouseClicked(event -> {
            handlePinPost();
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
        } else if (currentPageController instanceof CommunityPageController communityPageController) {
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
            postService.addSavedPost(user.getId());
            image = new ImageView("/src/view/images/SavedClickIcon.png");
            isSaved = true;
        } else {
            postService.removeSavedPost(user.getId());
            image = new ImageView("/src/view/images/SavedIcon.png");
            isSaved = false;
        }
        image.setFitHeight(20);
        image.setFitWidth(20);
        savePostButton.setGraphic(image);
    }

    private void handlePinPost() {
        ImageView image;
        if (!isPinned) {
            postService.addPinPost();
            image = new ImageView("/src/view/images/PinClickIcon.png");
            isPinned = true;
        } else {
            postService.removePinPost();
            image = new ImageView("/src/view/images/PinIcon.png");
            isPinned = false;
        }
        image.setFitHeight(20);
        image.setFitWidth(20);
        pinPostButton.setGraphic(image);

        refreshCommunityPage();
    }

    private void refreshCommunityPage() {
        Controller currentPageController = GuestContext.getCurrentController();
        if (currentPageController instanceof CommunityPageController communityPageController) {
            communityPageController.refreshPinnedPosts();
        }
    }

    private void setDataOnCard(Post post) throws SQLException {
        String communityTitle = postService.getCommunityTitle();
        community.setText("r/" + communityTitle);
        String nickname = postService.getNickname();
        username.setText(nickname);
        date.setText(formatter.getFormattedTime(post.getTime()));
        title.setText(post.getTitle());
        content.setText(post.getContent());
        scoreLabel.setText(post.getLikes() - post.getDislikes() + "");
        checkPostVisibility();
        setSavePostImage();
        setPinPostImage();
    }

    private void checkPostVisibility() throws SQLException {
        Guest guest = GuestContext.getCurrentGuest();

        if (guest.getRole() != Role.GUEST) {
            User user = (User) guest;
            reportPostButton.setVisible(!postService.isReported());
            reportPostButton.setManaged(!postService.isReported());
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
            isSaved = postService.isSaved(user.getId());
        } else {
            deletePostButton.setVisible(false);
            deletePostButton.setManaged(false);
            savePostButton.setVisible(false);
            savePostButton.setManaged(false);
        }

        User user = (guest instanceof User) ? (User) guest : null;
        boolean isAdmin = (user != null && postService.isUserAdminOfCommunity(user.getId(), postService.getPost().getCommunityId()));
        isPinned = postService.isPinned();
        if (isAdmin) {
            pinPostButton.setVisible(true);
            pinPostButton.setManaged(true);
            pinPostButton.setDisable(false);
        } else {
            if (isPinned) {
                pinPostButton.setVisible(true);
                pinPostButton.setManaged(true);
                pinPostButton.setDisable(true);
            } else {
                pinPostButton.setVisible(false);
                pinPostButton.setManaged(false);
            }
        }

    }

    public void setData(Post post) throws SQLException {
        myVBox.setMaxHeight(180);
        setDataOnCard(post);
        isOpenInPostPage = false;
    }

    public void setDataPostPage(Post post) throws SQLException {
        myVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
        setDataOnCard(post);
        isOpenInPostPage = true;
    }

    private void setButtonImage(Button button, boolean condition, String trueImagePath, String falseImagePath) {
        String imagePath = condition ? trueImagePath : falseImagePath;
        ImageView image = new ImageView(imagePath);
        image.setFitHeight(20);
        image.setFitWidth(20);
        button.setGraphic(image);
    }

    private void setSavePostImage() {
        setButtonImage(savePostButton, isSaved, "/src/view/images/SavedClickIcon.png", "/src/view/images/SavedIcon.png");
    }

    private void setPinPostImage() {
        setButtonImage(pinPostButton, isPinned, "/src/view/images/PinClickIcon.png", "/src/view/images/PinIcon.png");
    }

    private void goToPostPage() {
        String fxmlfile = "/src/view/fxml/PostPage.fxml";
        // TODO: cosa fare in questo caso?
        //PostPageController postPageController = PageControllerFactory.createPostPageController(postService.getPost());
        PostPageController postPageController = new PostPageController(postService);
        SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
        SceneManager.loadScene(fxmlfile, postPageController);
    }

    private void loadReplyBox() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Reply.fxml"));
            // TODO: review this method with attention
            PostReplyController postReplyController = ComponentFactory.createPostReplyController(this);
            fxmlLoader.setController(postReplyController);
            VBox replyBox = fxmlLoader.load();
            repliesContainer.getChildren().addFirst(replyBox);
            isReplying = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeReplyField() {
        isReplying = false;
        repliesContainer.getChildren().removeFirst();
    }

    public PostService getPostService() {
        return postService;
    }

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
