package src.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.businesslogic.FeedService;
import javafx.scene.control.Label;

import java.io.IOException;
import src.domainmodel.*;
import src.businesslogic.*;
import src.orm.ModeratorDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController implements Initializable  {

    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;

    User user;
    List<Post> posts;
    private FeedService feedService;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    private SearchService searchService = new SearchService();
    private ContextMenu suggestionsPopup = new ContextMenu();

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
                                return searchService.searchCommunities(newValue);
                            }
                        };

                        searchTask.setOnSucceeded(event -> {
                            List<Community> communities = searchTask.getValue();
                            // Popola il ContextMenu
                            suggestionsPopup.getItems().clear();
                            if (communities != null && !communities.isEmpty()) {
                                for (Community community : communities) {
                                    Label suggestionLabel = new Label(community.getTitle());
                                    suggestionLabel.prefWidthProperty().bind(searchField.widthProperty());
                                    CustomMenuItem item = new CustomMenuItem(suggestionLabel, true);
                                    // Quando si clicca il suggerimento carica CommunityPage
                                    item.setOnAction(e -> {
                                        searchField.setText(community.getTitle());
                                        suggestionsPopup.hide();
                                        // Carica la pagina della community
                                        loadCommunityPage(community);
                                    });
                                    suggestionsPopup.getItems().add(item);
                                }
                                Platform.runLater(() -> {
                                    if (searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                                        suggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
                                    }
                                });
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

   // @FXML
    //meglio fare un metodo generale per cambiare pagina
//    public static void openHomePage(User user, Stage stage) {
//        try {
//            System.out.println("Opening Home Page...");
//            FXMLLoader homePage = new FXMLLoader(HomePageController.class.getResource("/src/view/fxml/CommunityPage.fxml"));
//            homePage.setController(new CommunityController(new CommunityService(user.getId())));
//            stage.setScene(new Scene(homePage.load()));
//            stage.setTitle("Home Page");
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Error loading Home Page.");
//        }
//    }

    private void loadCommunityPage(Community community) {
        try {
            ModeratorDAO moderatorDAO = new ModeratorDAO();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/CommunityPage.fxml"));
            try {
                Moderator moderator = moderatorDAO.findById(1).orElse(null);
                loader.setController(new CommunityController(new CommunityService(community.getId()), moderator));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(community.getTitle());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}