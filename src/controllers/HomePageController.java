package src.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.businesslogic.FeedService;
import javafx.scene.control.Label;

import java.io.IOException;

import src.controllers.helpers.CommunitySearchHelper;
import src.domainmodel.*;
import src.businesslogic.*;
import src.servicemanager.GuestContext;
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

    private final SearchService searchService = new SearchService();

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

            searchField.setOnMouseClicked(e->{
                searchField.setEditable(true);
            });

            CreatePostButton.setOnMouseClicked(e ->{
                PostCreationPageController postCreationPageController = new PostCreationPageController(new PostCreationService());
                SceneManager.changeScene("postCreation","/src/view/fxml/PostCreationPage.fxml", postCreationPageController);
            });
            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

            CommunitySearchHelper communitySearchHelper = new CommunitySearchHelper(searchField,
                    searchService::searchCommunities, this::loadCommunityPage, community -> {
                try {
                    searchService.subscribeCommunity(community);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            communitySearchHelper.setupSearchListener();


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
        CreatePostButton.setVisible(!(GuestContext.getCurrentGuest().getRole() == Role.GUEST));
        System.out.println("Initializing data...");
        searchField.clear();
        searchField.setEditable(false);
        List<Post> post = feedService.getFeed();
        if(!(post ==null) ){
            posts = new ArrayList<>(feedService.getFeed());
            loadPosts(posts);
        }
    }
}