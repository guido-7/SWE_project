package src.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;
import src.businesslogic.SearchService;
import src.businesslogic.UserProfileService;
import src.controllers.factory.ComponentFactory;
import src.controllers.factory.PageControllerFactory;
import src.domainmodel.*;

import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;
import src.utils.LoadingPost;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class CommunityPageController implements Initializable, Controller {
    //nav bar
    @FXML
    private ImageView homePageButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button createPostButton;
    @FXML
    private ImageView userProfileAccess;

    // Setting items
    @FXML
    private MenuButton settingsButton;
    @FXML
    private MenuItem reportPageItem;
    @FXML
    private MenuItem rolePageItem;
    @FXML
    private MenuItem deleteCommunityItem;

    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Text community_title;
    @FXML
    private Text description;
    @FXML
    private Text num_subscribes;
    @FXML
    private Text num_monthly_visits;
    @FXML
    private VBox rulesContainer;
    @FXML
    private Label rules;
    @FXML
    private Button subscribeButton;
    @FXML
    private Button unsubscribeButton;
    @FXML
    private VBox TextNoRules;
    @FXML
    private Button AddRuleButton;
    @FXML
    private AnchorPane PopUpDeleteCommunityContainer;
    @FXML
    private VBox pinnedPostsContainer;

    private List<Post> posts;
    private final CommunityService communityService;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final SearchService searchService = new SearchService();
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private String currentSearchTerm = "";
    private final int communityId;

    public CommunityPageController(CommunityService communityService) {
        this.communityService = communityService;
        this.communityId = communityService.getCommunityId();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PopUpDeleteCommunityContainer.setVisible(false);
        PopUpDeleteCommunityContainer.setMouseTransparent(true);
        unsubscribeButton.setVisible(false);
        AddRuleButton.setVisible(false);
        userProfileAccess.setVisible(false);
        createPostButton.setVisible(false);
        settingsButton.setVisible(false);

        try {
            init_data();

            homePageButton.onMouseClickedProperty().set(event -> {
                SceneManager.changeScene("home", "/src/view/fxml/HomePage.fxml", null);
            });

            createPostButton.onMouseClickedProperty().set(event -> {
                openPostCreationPage();
            });

            userProfileAccess.onMouseClickedProperty().set(event -> {
                UserProfilePageController userProfilePageController = PageControllerFactory.createUserProfilePageController((User) GuestContext.getCurrentGuest());
                SceneManager.changeScene("UserProfilePage", "/src/view/fxml/UserProfilePage.fxml", userProfilePageController);
            });

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

            searchField.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    String searchTerm = searchField.getText().trim();
                    if (!searchTerm.isEmpty()) {
                        suggestionsPopup.hide();
                        showFilteredPosts(searchTerm);
                    }
                }
            });

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    suggestionsPopup.hide();
                    resetPosts();
                } else if (!newValue.equals(oldValue)) {
                    updateSuggestions(newValue);
                }
            });

            rules.setOnMouseClicked(event -> loadRules());

            subscribeButton.setOnMouseClicked(event -> {
                try {
                    if(communityService.subscribe()) {
                        setData(communityService.getCommunity());
                        subscribeButton.setVisible(false);
                        unsubscribeButton.setVisible(true);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            unsubscribeButton.setOnMouseClicked(event -> {
                try {
                    if (communityService.unsubscribe()) {
                        setData(communityService.getCommunity());
                        subscribeButton.setVisible(true);
                        unsubscribeButton.setVisible(false);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init_data() throws SQLException {
        searchField.clear();
        postsContainer.getChildren().clear();
        pinnedPostsContainer.getChildren().clear();
        Community currentCommunity = communityService.getCommunity();
        setData(currentCommunity);

        community_title.setOnMouseClicked(event -> {
            postsContainer.getChildren().clear();
            CommunityPageController communityPageController = PageControllerFactory.createCommunityPageController(communityId);
            SceneManager.changeScene("community " + communityId, "/src/view/fxml/CommunityPage.fxml", communityPageController);
        });

        if (communityService.isSubscribed()) {
            subscribeButton.setVisible(false);
            unsubscribeButton.setVisible(true);
        } else {
            subscribeButton.setVisible(true);
            unsubscribeButton.setVisible(false);
        }

        // get user role and set UI
        //GuestContext.setPreviousContextGuest(GuestContext.getCurrentGuest());
        Guest guest = retriveRightGuest();
        GuestContext.setCurrentGuest(guest);

        posts = new ArrayList<>(communityService.getPosts());
        loadPosts(posts);

        List<Integer> pinnedPosts = communityService.getPinnedPosts();
        Map<Integer, String> pinnedPostsTitle = new HashMap<>();
        for (Integer pinnedPostId : pinnedPosts) {
            pinnedPostsTitle.put(pinnedPostId, communityService.getPostTitle(pinnedPostId));
        }
        loadPinnedPost(pinnedPostsTitle);
    }

    private void handleSettingsClick() {
        CommunitySettingsPageController communitySettingsPageController = PageControllerFactory.createCommunitySettingsController(communityId);
        SceneManager.changeScene("community settings " + communityId, "/src/view/fxml/CommunitySettings.fxml", communitySettingsPageController);
    }

    private void openPostCreationPage() {
        PostCreationPageController postCreationController = PageControllerFactory.createPostCreationPageController();
        SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
        SceneManager.changeScene("post creation " + communityId, "/src/view/fxml/PostCreationPage.fxml", postCreationController);
    }

    private void handleRolePageClick() {
        AdminPageController adminPageController = PageControllerFactory.createAdminPageController(communityId);
        SceneManager.setPreviousScene(SceneManager.getPrimaryStage().getScene());
        SceneManager.changeScene("Admin Page " + communityId, "/src/view/fxml/AdminPage.fxml", adminPageController);
    }

    private void updateSuggestions(String searchTerm) {
        Task<List<Post>> searchTask = new Task<>() {
            @Override
            protected List<Post> call() {
                return searchService.searchPosts(searchTerm, communityService.getCommunityId());
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
        LoadingPost.LoadPosts(newPosts,postsContainer);
    }

    // Reset posts to the initial state after a search
    private void resetPosts() {
        currentSearchTerm = "";
        postsContainer.getChildren().clear();
        allPostsLoaded = false;
        try {
            posts = new ArrayList<>(communityService.getPosts());
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
                return communityService.getFilteredPosts(searchTerm);
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
                    return communityService.getNextPosts();
                } else {
                    return communityService.getNextFilteredPosts(currentSearchTerm);
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

    public void setData(Community community) {
        System.out.println("Setting data");
        community_title.setText(community.getTitle());
        description.setText(community.getDescription());
        num_subscribes.setText(community.getSubscribers() + "");
        num_monthly_visits.setText(community.getMonthlyVisits() + "");
    }

    @FXML
    public void loadRules() {
        try {
            List<Rule> rules = communityService.getCommunityRules();
            if (rules.isEmpty()) {
                Text text = new Text("No rules for this community");
                text.setStyle("-fx-fill: red; -fx-font-weight: bold;");
                TextNoRules.getChildren().add(text);

                PauseTransition pause = new PauseTransition(Duration.seconds(5));
                pause.setOnFinished(event -> TextNoRules.getChildren().remove(text));
                pause.play();
                return;
            }
            postsContainer.getChildren().clear();
            for (Rule rule : rules) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/RulesPage.fxml"));
                    RulesController rulesController = ComponentFactory.createRulesController(communityId, rule.getId());
                    fxmlLoader.setController(rulesController);
                    VBox vBox = fxmlLoader.load();
                    rulesController.setRuleData(rule.getTitle(), rule.getContent());
                    postsContainer.getChildren().add(vBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get the right guest for the page
    private Guest getCurrentGuest(Guest guest) throws SQLException {
        Role role = guest.getRole();
        if (Objects.requireNonNull(role) == Role.USER) {
            User user = (User) guest;
            userProfileAccess.setVisible(true);
            createPostButton.setVisible(true);

            Moderator communityModerator = communityService.getModerator(user.getId());
            if (communityModerator != null && communityModerator.getRole() == Role.MODERATOR) {
                updateModeratorUI();
                return communityModerator;
            }

            Admin admin = communityService.getAdmin(user.getId());
            if (admin != null && admin.getRole() == Role.ADMIN) {
                updateAdminUI();
                return admin;
            }
        }
        return guest;
    }

    public void loadPinnedPost(Map<Integer, String> postIds_title)  {
        if (postIds_title.isEmpty()) {
            return;
        }
        for(var key : postIds_title.keySet()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/PinnedPost.fxml"));
            PinnedPostController pinnedPostController = ComponentFactory.createPinnedPostController(communityId);
            fxmlLoader.setController(pinnedPostController);
            HBox pinnedPost = null;
            try {
                pinnedPost = fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pinnedPostController.setPostTitle(postIds_title.get(key));
            pinnedPostController.setPostId(key);
            pinnedPostsContainer.getChildren().add(pinnedPost);
        }
    }

    private void openConfirmationDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/ConfirmationDialog.fxml"));
        Parent popUp = loader.load();
        ConfirmationDialogPageController confirmationDialogPageController = loader.getController();
        confirmationDialogPageController.setQuestion("Do you really want to delete your community?");

        confirmationDialogPageController.setCallback(choice->{
            if(choice){
                communityService.deleteCommunity();
                SceneManager.changeScene("home", "/src/view/fxml/HomePage.fxml",null);
            }
            closePopup();
        });
        PopUpDeleteCommunityContainer.getChildren().clear();
        PopUpDeleteCommunityContainer.getChildren().add(popUp);
        PopUpDeleteCommunityContainer.setMouseTransparent(false);
        PopUpDeleteCommunityContainer.setVisible(true);
    }

    private void closePopup() {
        PopUpDeleteCommunityContainer.getChildren().clear();
        PopUpDeleteCommunityContainer.setMouseTransparent(true);
        PopUpDeleteCommunityContainer.setVisible(false);
    }

    private void handleAddRuleClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/AddRule.fxml"));
            AddRuleController addRuleController = PageControllerFactory.createAddRuleController(communityId);
            loader.setController(addRuleController);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshPinnedPosts() {
        pinnedPostsContainer.getChildren().clear();
        Map<Integer, String> pinnedPosts = new HashMap<>();

        for (Integer pinnedPostId : communityService.getPinnedPosts()) {
            try {
                pinnedPosts.put(pinnedPostId, communityService.getPostTitle(pinnedPostId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        loadPinnedPost(pinnedPosts);
    }

    private Guest retriveRightGuest() throws SQLException {
        return getCurrentGuest(GuestContext.getCurrentGuest());
    }

    private void updateModeratorUI(){
        settingsButton.setVisible(true);
        reportPageItem.setVisible(true);
        rolePageItem.setVisible(false);
        deleteCommunityItem.setVisible(false);

        reportPageItem.setOnAction(event -> {
            handleSettingsClick();
        });
    }

    private void updateAdminUI() {
        updateModeratorUI();
        rolePageItem.setVisible(true);
        deleteCommunityItem.setVisible(true);

        AddRuleButton.setVisible(true);
        AddRuleButton.setOnMouseClicked(event->handleAddRuleClick());

        deleteCommunityItem.setOnAction(event -> {
            try {
                openConfirmationDialog();
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        rolePageItem.setOnAction(event -> {
            handleRolePageClick();
        });
    }

    public VBox getPostsContainer() {
        return postsContainer;
    }

}
