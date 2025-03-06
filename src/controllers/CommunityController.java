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
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import src.FunctionalInterfaces.BanUserCallback;
import src.businesslogic.CommunityService;
import src.businesslogic.PostService;
import src.businesslogic.SearchService;
import src.businesslogic.UserProfileService;
import src.domainmodel.*;

import src.servicemanager.GuestContext;
import src.servicemanager.SceneManager;
import src.utils.LoadingPost;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
public class CommunityController implements Initializable, Controller {

    @FXML
    private VBox postsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;
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
    private ImageView settings;
    @FXML
    private ImageView userProfileAccess;
    @FXML
    private ImageView homePageButton;
    @FXML
    private Button subscribeButton;
    @FXML
    private Button unsubscribeButton;
    @FXML
    private VBox TextNoRules;
    @FXML
    private Button AddRuleButton;
    @FXML
    private Button deleteCommunityButton;
    @FXML
    private AnchorPane PopUpDeleteCommunityContainer;

    private List<Post> posts;
    private final CommunityService communityservice;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final SearchService searchService = new SearchService();
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private String currentSearchTerm = "";
    private int currentCommunityId;

    public CommunityController(CommunityService communityService) {
        this.communityservice = communityService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PopUpDeleteCommunityContainer.setVisible(false);
        PopUpDeleteCommunityContainer.setMouseTransparent(true);
        deleteCommunityButton.setVisible(false);
        unsubscribeButton.setVisible(false);
        AddRuleButton.setVisible(false);
        settings.setVisible(false);
        userProfileAccess.setVisible(false);
        this.currentCommunityId = communityservice.getCommunityId();

        try {
            init_data();

            homePageButton.onMouseClickedProperty().set(event -> {
                SceneManager.changeScene("home", "/src/view/fxml/HomePage.fxml",null);
            });

            userProfileAccess.onMouseClickedProperty().set(event -> {
                UserProfileService userProfileService = new UserProfileService((User) GuestContext.getCurrentGuest());
                UserProfilePageController userProfilePageController = new UserProfilePageController(userProfileService);
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

            settings.setOnMouseClicked(event -> {
                handleSettingsClick();
            });

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    suggestionsPopup.hide();
                    resetPosts();
                } else if (!newValue.equals(oldValue)) {
                    updateSuggestions(newValue);
                }
            });

            rules.setOnMouseClicked(event -> loadRules(currentCommunityId));

            subscribeButton.setOnMouseClicked(event -> {
                try {
                    if(communityservice.subscribe()) {
                        setData(communityservice.getCommunity());
                        subscribeButton.setVisible(false);
                        unsubscribeButton.setVisible(true);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            unsubscribeButton.setOnMouseClicked(event -> {
                try {
                    if (communityservice.unsubscribe()) {
                        setData(communityservice.getCommunity());
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
    @FXML
    private void handleSettingsClick() {
        CommunitySettingsController communitySettingsController = new  CommunitySettingsController(communityservice);
        GuestContext.setCurrentController(communitySettingsController);
        SceneManager.changeScene("community settings " + communityservice.getCommunityId(), "/src/view/fxml/CommunitySettings.fxml", communitySettingsController);
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
        LoadingPost.LoadPosts(newPosts,postsContainer);
    }

    // Reset posts to the initial state after a search
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

    public void setData(Community community) {
        System.out.println("Setting data");
        community_title.setText(community.getTitle());
        description.setText(community.getDescription());
        num_subscribes.setText(community.getSubscribers() + "");
        num_monthly_visits.setText(community.getMonthlyVisits() + "");
    }

    @FXML
    public void loadRules(int communityId) {
        try {

            List<Rule> rules = communityservice.getCommunityRules(communityId);
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
                    RulesController rulesController = new RulesController(communityservice,rule.getId());
                    fxmlLoader.setController(rulesController);
                    VBox vBox = fxmlLoader.load();
                    rulesController.setRuleData(community_title.getText(), rule.getTitle(), rule.getContent());
                    postsContainer.getChildren().add(vBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Guest getCurrentGuest(Guest guest) throws SQLException {
        Role role = guest.getRole();
        if (Objects.requireNonNull(role) == Role.USER) {
            User user = (User) guest;
            userProfileAccess.setVisible(true);
            userProfileAccess.setVisible(true);

            Moderator communityModerator = communityservice.getModerator(user.getId());

            if (communityModerator != null && communityModerator.getRole() == Role.MODERATOR) {
                updateModeratorUI();
                return communityModerator;
            } else {
                Admin admin = communityservice.isAdmin(user.getId());
                if (admin != null && admin.getRole() == Role.ADMIN) {
                    updateAdminUI();
                    return admin;
                }

                return guest;
            }

        }
        return guest;
    }

    private void updateAdminUI() {
        updateModeratorUI();
        AddRuleButton.setVisible(true);
        AddRuleButton.setOnMouseClicked(event->handleAddRuleClick());
        deleteCommunityButton.setVisible(true);
        deleteCommunityButton.setOnMouseClicked(event -> {
            try {
                openConfirmationDialog();

            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


    }

    private void openConfirmationDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/ConfirmationDialog.fxml"));
        Parent popUp = loader.load();
        ConfirmationDialogPageController confirmationDialogPageController = loader.getController();
        confirmationDialogPageController.setQuestion("Do you really want to delete your community?");

        confirmationDialogPageController.setCallback(choice->{
            if(choice){
                communityservice.deleteCommunity();
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
            AddRuleController addRuleController = new AddRuleController(communityservice);
            loader.setController(addRuleController);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Guest retriveRightGuest() throws SQLException {
        return getCurrentGuest(GuestContext.getCurrentGuest());
    }

    private void updateModeratorUI(){
        settings.setVisible(true);
    }

    public VBox getPostsContainer() {
        return postsContainer;
    }

    @Override
    public void init_data() throws SQLException {
        searchField.clear();
        Community currentCommunity = communityservice.getCommunity();
        setData(currentCommunity);

        community_title.setOnMouseClicked(event -> {
            postsContainer.getChildren().clear();
            SceneManager.changeScene("community " + currentCommunityId, "/src/view/fxml/CommunityPage.fxml", new CommunityController(communityservice));
        });

        if (communityservice.isSubscribed()) {
            subscribeButton.setVisible(false);
            unsubscribeButton.setVisible(true);
        } else {
            subscribeButton.setVisible(true);
            unsubscribeButton.setVisible(false);
        }

        // get the guest for the page
        Guest guest = retriveRightGuest();
        GuestContext.setCurrentGuest(guest);

        posts = new ArrayList<>(communityservice.getPosts());
        loadPosts(posts);
    }

}




