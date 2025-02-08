package src.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import src.domainmodel.*;
import src.businesslogic.*;
import src.servicemanager.Service;

import java.awt.event.ActionEvent;
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
    @FXML
    private TextField searchField;

    List<Post> posts;
    private FeedService feedService;
    private SearchCommunityService searchCommunityService = new SearchCommunityService();
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    // ContextMenu per i suggerimenti
    private ContextMenu suggestionsPopup = new ContextMenu();

    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            setFeedService(Service.getFeedService());
            posts = new ArrayList<>(feedService.getFeed());
            loadPosts(posts);

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

            searchField.textProperty().addListener(new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue.isEmpty()) {
                        suggestionsPopup.hide();
                    } else {
                        // Avvia una ricerca in background per ottenere le community che corrispondono al testo
                        Task<List<Community>> searchTask = new Task<>() {
                            @Override
                            protected List<Community> call() {
                                return searchCommunityService.searchCommunities(newValue);
                            }
                        };

                        searchTask.setOnSucceeded(event -> {
                            List<Community> communities = searchTask.getValue();
                            // Popola il ContextMenu
                            suggestionsPopup.getItems().clear();
                            if (communities != null && !communities.isEmpty()) {
                                for (Community community : communities) {
                                    MenuItem item = new MenuItem(community.getTitle());
                                    // Quando si clicca il suggerimento, ad esempio, imposta il testo e/o naviga alla community
                                    item.setOnAction(e -> {
                                        searchField.setText(community.getTitle());
                                        suggestionsPopup.hide();
                                        // Aggiungi eventuale logica: per esempio, carica la pagina della community
                                    });
                                    suggestionsPopup.getItems().add(item);
                                }
                                // Mostra il ContextMenu sotto il TextField
                                suggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
                            } else {
                                suggestionsPopup.hide();
                            }
                        });

                        searchTask.setOnFailed(event -> {
                            suggestionsPopup.hide();
                        });

                        new Thread(searchTask).start();
                    }
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

        // Mostra indicatore di caricamento
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

}
