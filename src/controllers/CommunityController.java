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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;
import src.businesslogic.SearchService;
import src.domainmodel.*;

import src.servicemanager.GuestContext;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

    private List<Post> posts;
    private final CommunityService communityservice;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final SearchService searchService = new SearchService();
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private String currentSearchTerm = "";
    //private SearchService searchCommunityService = new SearchService();
    private int currentCommunityId;


    public CommunityController(CommunityService communityService) {
        this.communityservice = communityService;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        settings.setVisible(false);
        this.currentCommunityId = communityservice.getCommunityId();

        try {

            init_data();

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0 && !isLoading && !allPostsLoaded) {
                    loadMorePosts();
                }
            });

            // Search field event handlers
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

            // Search field suggestions
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    suggestionsPopup.hide();
                    resetPosts();
                } else if (!newValue.equals(oldValue)) {
                    updateSuggestions(newValue);
                }
            });

            rules.setOnMouseClicked(event -> loadRules(currentCommunityId));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleSettingsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/CommunitySettings.fxml"));
            loader.setController(new CommunitySettingsController(communityservice));
            Parent root = loader.load();
            Stage stage = (Stage) settings.getScene().getWindow();
            stage.setTitle("Community Settings");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
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

    private void loadCommunityPage(Community community) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/CommunityPage.fxml"));
            loader.setController(new CommunityController(new CommunityService(community.getId())));
            Parent root = loader.load();
            loadRules(community.getId()); // Passa l'ID della community per caricare le regole
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(community.getTitle());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            postsContainer.getChildren().clear();
            for (Rule rule : rules) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/src/view/fxml/RulesPage.fxml"));
                    RulesController rulesController = new RulesController();
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
        if (guest.getRole() == Role.USER) {
            User user = (User) guest;

            Moderator moderator = communityservice.getModerator(user.getId());
            if (moderator != null && moderator.getRole() == Role.MODERATOR) {

                updateUI();

                return moderator;
            } else return guest;
        }
        return guest;
    }
    private Guest retriveRightGuest() throws SQLException {
        return getCurrentGuest(GuestContext.getCurrentGuest());

    }
    private void updateUI(){
        settings.setVisible(true);
    }


    @Override
    public void init_data() throws SQLException {

        Community currentCommunity = communityservice.getCommunity();
        setData(currentCommunity);

        // get the guest for the page
        Guest guest = retriveRightGuest();
        GuestContext.setCurrentGuest(guest);


        posts = new ArrayList<>(communityservice.getPosts());
        loadPosts(posts);


    }
}




