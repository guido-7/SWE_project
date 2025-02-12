package src.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import src.businesslogic.CommunityService;
import src.businesslogic.SearchService;
import src.domainmodel.Post;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CommunityController implements Initializable {

    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;

    private List<Post> posts;
    private final CommunityService communityservice;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final SearchService searchService = new SearchService();
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private String currentSearchTerm = "";

    public CommunityController(CommunityService communityService) {
        this.communityservice = communityService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            posts = new ArrayList<>(communityservice.getPosts());
            loadPosts(posts);

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

            // Gestione del tasto invio con priorità massima
            searchField.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume(); // Previene che l'evento si propaghi
                    String searchTerm = searchField.getText().trim();
                    System.out.println("ENTER premuto. Search term: " + searchTerm);
                    if (!searchTerm.isEmpty()) {
                        suggestionsPopup.hide();
                        showFilteredPosts(searchTerm);
                    }
                }
            });

            // Gestione del cambio testo per i suggerimenti
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    suggestionsPopup.hide();
                    resetPosts();
                } else if (!newValue.equals(oldValue)) {
                    updateSuggestions(newValue);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSuggestions(String searchTerm) {
        Task<List<Post>> searchTask = new Task<>() {
            @Override
            protected List<Post> call() {
                return searchService.searchPosts(searchTerm, communityservice.getCommunityId());
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<Post> searchResults = searchTask.getValue();
            suggestionsPopup.getItems().clear();

            if (searchResults != null && !searchResults.isEmpty()) {
                for (Post post : searchResults) {
                    Label suggestionLabel = new Label(post.getTitle());
                    suggestionLabel.prefWidthProperty().bind(searchField.widthProperty());
                    CustomMenuItem item = new CustomMenuItem(suggestionLabel, true);
                    item.setOnAction(e -> {
                        searchField.setText(post.getTitle());
                        suggestionsPopup.hide();
                        showFilteredPosts(post.getTitle());
                    });
                    suggestionsPopup.getItems().add(item);
                }

                if (!searchField.getText().isEmpty() && searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                    suggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
                }
            } else {
                suggestionsPopup.hide();
            }
        });

        searchTask.setOnFailed(event -> {
            suggestionsPopup.hide();
        });

        new Thread(searchTask).start();
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

    private void resetPosts() {
        currentSearchTerm = "";
        postsContainer.getChildren().clear();
        allPostsLoaded = false;
        try {
            posts = new ArrayList<>(communityservice.getPosts());
            loadPosts(posts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFilteredPosts(String searchTerm) {
        currentSearchTerm = searchTerm;
        postsContainer.getChildren().clear();
        allPostsLoaded = false;

        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() {
                return communityservice.getFilteredPosts(searchTerm);
            }
        };

        task.setOnSucceeded(event -> {
            List<Post> filteredPosts = task.getValue();
            loadPosts(filteredPosts);

            // Se non ci sono risultati, mostra un messaggio
            if (filteredPosts.isEmpty()) {
                Label noResults = new Label("No posts found for: " + searchTerm);
                postsContainer.getChildren().add(noResults);
            }
        });

        task.setOnFailed(event -> {
            System.err.println("Failed to load filtered posts");
        });

        new Thread(task).start();
    }

    private void loadMorePosts() {
        isLoading = true;

        if (!postsContainer.getChildren().contains(progressIndicator)) {
            postsContainer.getChildren().add(progressIndicator);
        }

        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() {
                if (currentSearchTerm.isEmpty()) {
                    return communityservice.getNextPosts();
                } else {
                    return communityservice.getNextFilteredPosts(currentSearchTerm);
                }
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
            System.err.println("Failed to load more posts");
        });

        new Thread(task).start();
    }
}