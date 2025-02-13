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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.businesslogic.CommunityService;
import src.businesslogic.SearchCommunityService;
import src.domainmodel.Community;
import src.domainmodel.Post;

import javafx.scene.input.MouseEvent;
import src.domainmodel.Rule;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CommunityController implements Initializable {

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


    List<Post> posts;
    private final CommunityService communityservice;
    private boolean isLoading = false;
    private boolean allPostsLoaded = false;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private ContextMenu suggestionsPopup = new ContextMenu();
    private SearchCommunityService searchCommunityService = new SearchCommunityService();
    private int currentCommunityId;

    public CommunityController(CommunityService communityService) {
        this.communityservice = communityService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            Community currentCommunity = communityservice.getCommunity();
            setData(currentCommunity);
            this.currentCommunityId = currentCommunity.getId();
            posts = new ArrayList<>(communityservice.getPosts());
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

            rules.setOnMouseClicked(event -> loadRules(currentCommunityId));

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
                return communityservice.getNextPosts();
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




}