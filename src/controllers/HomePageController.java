package src.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.businesslogic.FeedService;
import javafx.scene.control.Label;
import src.domainmodel.Post;

import java.io.IOException;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;


    List<Post> posts;
    private FeedService feedService;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    public HomePageController(FeedService feedService) {
        this.feedService = feedService;
    }
    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            posts = new ArrayList<>(feedService.getFeed());
            loadPosts(posts);

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadPosts(List<Post> newPosts) {
        for (Post post : newPosts) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/Post.fxml"));
                VBox vBox = fxmlLoader.load();
                PostController postController = fxmlLoader.getController();
                postController.setData(post);
                postsContainer.getChildren().add(vBox);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMorePosts() {
        isLoading = true;

        if (!postsContainer.getChildren().contains(progressIndicator)) {
            postsContainer.getChildren().add(progressIndicator);
        }

        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() {
                return feedService.getNextFeed();
            }
        };

        task.setOnSucceeded(event -> {
            List<Post> newPosts = task.getValue();
            postsContainer.getChildren().remove(progressIndicator);

            if (newPosts.isEmpty()) {
                allPostsLoaded = true;
                Label noMoreContent = new Label("No more content available");
                postsContainer.getChildren().add(noMoreContent);
            } else {
                loadPosts(newPosts);
            }

            isLoading = false;
        });

        task.setOnFailed(event -> {
            isLoading = false;
            postsContainer.getChildren().remove(progressIndicator);
        });

        new Thread(task).start();
    }

    @FXML
    public static void openHomePage(Stage stage) {
        try {
            System.out.println("Opening Home Page...");
            FXMLLoader homePage = new FXMLLoader(HomePageController.class.getResource("/src/view/fxml/HomePage.fxml"));
            stage.setScene(new Scene(homePage.load()));
            stage.setTitle("Home Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Home Page.");
        }
    }

}
