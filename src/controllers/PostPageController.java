package src.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import src.businesslogic.CommentService;
import src.businesslogic.PostService;
import src.domainmodel.Comment;
import src.domainmodel.Post;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PostPageController implements Controller, Initializable {
    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;

    PostService postService;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;

    public PostPageController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            init_data();
            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMoreRootComments();
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPost(Post post) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Post.fxml"));
            PostController postController = new PostController(new PostService(post));
            fxmlLoader.setController(postController);
            VBox vBox = fxmlLoader.load();
            postController.setData(post);
            postsContainer.getChildren().add(vBox);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRootComments(List<Comment> rootComments) {
        for (Comment comment : rootComments) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Comment.fxml"));
                CommentController commentController = new CommentController(new CommentService(comment));
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
    }

    @Override
    public void init_data() throws SQLException {
        Post currentPost = postService.getPost();
        loadPost(currentPost);

        List<Comment> rootComments = postService.getRootComments();
       loadRootComments(rootComments);
    }
}
