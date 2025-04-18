package src.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;

import src.controllers.factory.PageControllerFactory;
import src.controllers.helpers.CommunitySearchHelper;
import src.domainmodel.*;
import src.businesslogic.*;
import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;
import src.utils.LoadingPost;

import java.net.URL;
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
    private ImageView userProfileAccess;
    @FXML
    private Button createCommunityButton;
    @FXML
    private Button createPostButton;

    List<Post> posts;
    private FeedService feedService;
    private Boolean isLoading = false;
    private Boolean allPostsLoaded = false;
    private CommunitySearchHelper communitySearchHelper;
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    private final SearchService searchService = new SearchService();

    public HomePageController(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userProfileAccess.setVisible(false);
        userProfileAccess.setManaged(false);
        createCommunityButton.setVisible(false);
        createCommunityButton.setManaged(false);

        try {
            init_data();

            createCommunityButton.setOnMouseClicked(e -> openCommunityCreationPage());

            searchField.setOnMouseClicked(e -> searchField.setEditable(true));

            createPostButton.setOnMouseClicked(e -> openPostCreationPage());

            login.setOnMouseClicked(e -> handleLoginButton());

            userProfileAccess.setOnMouseClicked(e -> {
                try {
                    openProfilePage();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

            communitySearchHelper = new CommunitySearchHelper(searchField, searchService::searchCommunities, this::loadCommunityPage);
            communitySearchHelper.setupSearchListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPosts(List<Post> newPosts) {
        LoadingPost.LoadPosts(newPosts,postsContainer);
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
        // TODO: review
        if(!communityService.checkBannedUser()) {
            CommunityPageController communityPageController = PageControllerFactory.createCommunityPageController(community.getId());
            SceneManager.changeScene("community " + community.getId(), "/src/view/fxml/CommunityPage.fxml", communityPageController);
            //SceneManager.changeScene("community " + community.getId(), "/src/view/fxml/CommunityPage.fxml", new CommunityPageController(communityService));
        } else {
            Stage stage = (Stage) searchField.getScene().getWindow();
            BannedService bannedService = new BannedService(community.getId());
            SceneManager.openModal("banned", "/src/view/fxml/BannedMessage.fxml", new BannedController(bannedService),stage);
        }
    }

    private void openCommunityCreationPage() {
        CommunityCreationPageController communityCreationPageController = PageControllerFactory.createCommunityCreationPageController();
        SceneManager.changeScene("Community creation", "/src/view/fxml/CommunityCreationPage.fxml", communityCreationPageController);
    }

    private void openPostCreationPage() {
        SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
        SceneManager.changeScene("postCreation", "/src/view/fxml/PostCreationPage.fxml", PageControllerFactory.createPostCreationPageController());
    }

    public void handleLoginButton() {
        System.out.println("Login button clicked!");
        LoginController loginController = new LoginController(new LoginService());
        Stage stage = (Stage) searchField.getScene().getWindow();
        SceneManager.openModal("login", "/src/view/fxml/Login.fxml", loginController, stage);
    }

    public void setLoginButtonVisibility(boolean visibility){
        login.setVisible(visibility);
        login.setManaged(visibility);
    }

    public void openProfilePage() throws IOException {
        User user = (User) feedService.getGuest();
        UserProfilePageController userProfilePageController = PageControllerFactory.createUserProfilePageController(user);
        SceneManager.changeScene("profile", "/src/view/fxml/UserProfilePage.fxml", userProfilePageController);
    }

    public void setUserProfileAccessVisibility(boolean visibility){
        userProfileAccess.setVisible(visibility);
        userProfileAccess.setManaged(visibility);
    }

    @Override
    public void init_data() {
        postsContainer.getChildren().clear();
        boolean isUser = !(GuestContext.getCurrentGuest().getRole() == Role.GUEST);
        createPostButton.setVisible(isUser);
        createPostButton.setManaged(isUser);
        createCommunityButton.setVisible(isUser);
        createCommunityButton.setManaged(isUser);
        System.out.println("Initializing data...");
        searchField.clear();
        searchField.setEditable(false);
        List<Post> post = feedService.getFeed();
        if (!(post == null)) {
            posts = new ArrayList<>(feedService.getFeed());
            loadPosts(posts);
        }
    }

    public VBox getPostsContainer(){
        return postsContainer;
    }

    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    // For test
    public CommunitySearchHelper getCommunitySearchHelper(){
        return communitySearchHelper;
    }

}
