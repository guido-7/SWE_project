package src.utils;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import src.businesslogic.PostService;
import src.controllers.PostController;
import src.controllers.factory.ComponentFactory;
import src.domainmodel.Post;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class LoadingPost {

    public static void LoadPosts(List<Post> newPosts , VBox postsContainer) {
        for (Post post : newPosts) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(LoadingPost.class.getResource("/src/view/fxml/Post.fxml"));
                PostController postController = ComponentFactory.createPostController(post);
                fxmlLoader.setController(postController);
                VBox vBox = fxmlLoader.load();
                postController.setData(post);
                postsContainer.getChildren().add(vBox);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // TODO: review this method with attention
    public static void loadMorePosts(final Boolean isLoading,final Boolean allPostsLoaded, VBox postsContainer, Label progressIndicator, List<Post> newPost) {
        final boolean[] isLoadingWrapper = {isLoading};
        final boolean[] allPostsLoadedWrapper = {allPostsLoaded};

        isLoadingWrapper[0] = true;

        if (!postsContainer.getChildren().contains(progressIndicator)) {
            postsContainer.getChildren().add(progressIndicator);
        }

        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() {
                return newPost;
            }
        };

        task.setOnSucceeded(event -> {
            List<Post> newPosts = task.getValue();
            postsContainer.getChildren().remove(progressIndicator);

            if (newPosts.isEmpty()) {
                allPostsLoadedWrapper[0] = true;
                Label noMoreContent = new Label("No more content available");
                postsContainer.getChildren().add(noMoreContent);
            } else {
                LoadPosts(newPosts, postsContainer);
            }

            isLoadingWrapper[0] = false;
        });

        task.setOnFailed(event -> {
            isLoadingWrapper[0] = false;
            postsContainer.getChildren().remove(progressIndicator);
        });

        new Thread(task).start();
    }

}
