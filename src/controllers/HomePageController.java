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
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.businesslogic.FeedService;
import javafx.scene.control.Label;

import java.io.IOException;
import src.domainmodel.*;
import src.businesslogic.*;
import src.servicemanager.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController implements Initializable,Controller  {

    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;
    @FXML
    private Button login;
    @FXML
    ImageView userProfileAccess;
    @FXML
    private Button CreatePostButton;

    User user;
    List<Post> posts;
    private FeedService feedService;
    private Boolean isLoading = false;
    private Boolean allPostsLoaded = false;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    private SearchService searchService = new SearchService();
    private ContextMenu suggestionsPopup = new ContextMenu();

    public HomePageController(){}

    public HomePageController(FeedService feedService) {
        this.feedService = feedService;
    }
    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userProfileAccess.setVisible(false);

        try {
            init_data();
            CreatePostButton.setOnMouseClicked(e ->{
                PostCreationPageController postCreationPageController = new PostCreationPageController();
                SceneManager.changeScene("postCreation","/src/view/fxml/PostCreationPage.fxml", postCreationPageController);
            });
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
                PostController postController = new PostController(new PostService(post));
                fxmlLoader.setController(postController);
                VBox vBox = fxmlLoader.load();
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

    public void LoadUserPosts(){
        posts = new ArrayList<>(feedService.getFeed());
        loadPosts(posts);
    }

    private void loadCommunityPage(Community community){
        CommunityService communityService = new CommunityService(community.getId());
        SceneManager.changeScene("community " + community.getId(), "/src/view/fxml/CommunityPage.fxml", new CommunityController(communityService));
    }

    public void handleLoginButton() {
        System.out.println("Login button clicked!");
        LoginController loginController = new LoginController();
        loginController.setHomePageController(this);
        Stage stage = (Stage) searchField.getScene().getWindow();
        SceneManager.openModal("login", "/src/view/fxml/Login.fxml", loginController, stage);
    }

    public void setLoginButtonVisibility(boolean visibility){
        login.setVisible(visibility);
    }

    public void openProfilePage() throws IOException {
        UserProfileService userProfileService = new UserProfileService((User) feedService.getGuest());
        SceneManager.changeScene("profile", "/src/view/fxml/UserProfilePage.fxml", new UserProfilePageController(userProfileService));
    }

    public void setUserProfileAccessVisibility(boolean visibility){
        userProfileAccess.setVisible(visibility);
    }

    @Override
    public void init_data() {
        if (scrollPane.getContent() instanceof Pane) {
            ((Pane) scrollPane.getContent()).getChildren().clear();
        }

        System.out.println("Initializing data...");
        searchField.clear();
        List<Post> post = feedService.getFeed();
        if(!(post ==null) ){
            posts = new ArrayList<>(feedService.getFeed());
            loadPosts(posts);
        }
    }
}