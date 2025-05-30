package src.controllers.pagecontrollers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import src.services.componentservices.PostService;
import src.controllers.PageController;
import src.controllers.componentcontrollers.CommentController;
import src.controllers.componentcontrollers.PostController;
import src.factory.ComponentFactory;
import src.domainmodel.Comment;
import src.domainmodel.Post;
import src.usersession.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PostPageController implements PageController, Initializable {
    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;
    @FXML
    private ImageView homePageButton;
    @FXML
    private Button exitButton;

    PostService postService;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    public PostPageController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init_data();
        setOnEvent();
    }

    @Override
    public void setOnEvent() {
        homePageButton.onMouseClickedProperty().set(event -> {
            SceneManager.changeScene("home", "/src/view/fxml/HomePage.fxml", null);
        });

        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                loadMoreRootComments();
            }
        });

        exitButton.setOnMouseClicked(event -> {
            try {
                SceneManager.loadPreviousScene();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });


    }

    private void loadPost(Post post) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Post.fxml"));
            PostController postController = ComponentFactory.createPostController(post);
            fxmlLoader.setController(postController);
            VBox vBox = fxmlLoader.load();
            postController.setDataPostPage(post);
            postsContainer.getChildren().add(vBox);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRootComments(List<Comment> rootComments) {
        for (Comment comment : rootComments) {
            try {
                comment.setPost(postService.getPost());
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Comment.fxml"));
                CommentController commentController = ComponentFactory.createCommentController(comment);
                fxmlLoader.setController(commentController);
                VBox vBox = fxmlLoader.load();
                commentController.setData(comment);
                postsContainer.getChildren().add(vBox);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMoreRootComments() {
        isLoading = true;
        if (!postsContainer.getChildren().contains(progressIndicator)) {
            postsContainer.getChildren().add(progressIndicator);
        }

        Task<List<Comment>> task = new Task<>() {
            @Override
            protected List<Comment> call() {
                return postService.getNextRootComments();
            }
        };

        task.setOnSucceeded(event -> {
            List<Comment> newComments = task.getValue();
            postsContainer.getChildren().remove(progressIndicator);
            if (newComments.isEmpty()) {
                allPostsLoaded = true;
                Label noMoreContent = new Label("No more comment available");
                postsContainer.getChildren().add(noMoreContent);
            } else {
                loadRootComments(newComments);
            }
            isLoading = false;
        });

        task.setOnFailed(event -> {
            isLoading = false;
            postsContainer.getChildren().remove(progressIndicator);
        });
        new Thread(task).start();
    }

    @Override
    public void init_data(){
        postsContainer.getChildren().clear();
        Post currentPost = postService.getPost();
        loadPost(currentPost);

        allPostsLoaded = false;
        List<Comment> rootComments = postService.getRootComments();
        loadRootComments(rootComments);
    }

}
