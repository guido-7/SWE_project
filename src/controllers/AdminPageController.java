package src.controllers;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import src.businesslogic.CommunityService;
import src.servicemanager.FormattedTime;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class AdminPageController implements Initializable, Controller {
    @FXML
    private Pane BCPane;
    @FXML
    private Pane BLPane;
    @FXML
    private Pane BRPane;
    @FXML
    private Pane CCPane;
    @FXML
    private Pane CLPane;
    @FXML
    private Pane CRPane;
    @FXML
    private Button DismissButton;
    @FXML
    private Button PromoteButton;
    @FXML
    private TextField SearchSubsBar;
    @FXML
    private Pane TCPane;
    @FXML
    private Pane TLPane;
    @FXML
    private Pane TRPane;
    @FXML
    private AnchorPane SubInfoContainer;

    private boolean allPostsLoaded = false;
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private String currentSearchTerm = "";

    List<Pair<AnchorPane,UserInfoComponentController>> subInfoVector = new ArrayList<>();
    Pair<AnchorPane,UserInfoComponentController> deletingPair;
    final Map<Integer,Pane> allpane = new HashMap<>();
    private int  subscriberId;
    private final CommunityService communityService;

    public AdminPageController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PromoteButton.setVisible(false);
        PromoteButton.setManaged(false);
        DismissButton.setVisible(false);
        DismissButton.setManaged(false);
        ArrayList<Pane> paneGrid = new ArrayList<>(List.of(TLPane, TCPane, TRPane, CLPane, CCPane, CRPane, BLPane, BCPane, BRPane));

        //from 0 to 8
        //initialize all the grid and the bi-map
        for (int i = 0 ; i < paneGrid.size();i++){
            allpane.put(i,paneGrid.get(i));
        }

        try {
            init_data();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init_data() throws SQLException {
        int maxSubscribedNo = 9;
        Object[][] subscribedInfos;
        try {
            ArrayList<Integer> subIds = communityService.getSubs(maxSubscribedNo);
            if(!subIds.isEmpty()) {
                // [[nickname,data],[nickname2,data2]]
                subscribedInfos = communityService.getSubscribedData(subIds);

                for (int i = 0 ; i < subIds.size() && i < allpane.size(); i++) {
                    int subId = subIds.get(i);
                    subInfoVector.add(loadUserInfoComponent(subscribedInfos[i], subId));
                }
                buildGrid();
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        PromoteButton.setOnMouseClicked(event->{
            try {
                communityService.promote(subscriberId);
                removeFromGrid();
                PromoteButton.setVisible(false);
                PromoteButton.setManaged(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        DismissButton.setOnMouseClicked(event->{
            communityService.dismiss(subscriberId);
            removeFromGrid();
            DismissButton.setVisible(false);
            DismissButton.setManaged(false);
        });

        SearchSubsBar.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                String searchTerm = SearchSubsBar.getText().trim();
                if (!searchTerm.isEmpty()) {
                    suggestionsPopup.hide();
                    showFilteredSubs(searchTerm);
                }
            }
        });

        SearchSubsBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                suggestionsPopup.hide();
                //resetPosts();
            } else if (!newValue.equals(oldValue)) {
                updateSuggestions(newValue);
            }
        });
    }

    private void updateSuggestions(String newValue) {
        Task<Map<Integer,String>> searchTask = new Task<>() {
            @Override
            protected  Map<Integer,String> call() {
                return communityService.getFilteredSubs(newValue);
            }
        };

        searchTask.setOnSucceeded(event -> {
            Map<Integer,String> searchResults = searchTask.getValue();
            suggestionsPopup.getItems().clear();

            if (searchResults != null && !searchResults.isEmpty()) {
                for (Integer subId : searchResults.keySet()) {
                    String subNickname = searchResults.get(subId);
                    Label suggestionLabel = new Label(subNickname);
                    suggestionLabel.prefWidthProperty().bind(SearchSubsBar.widthProperty());
                    CustomMenuItem item = new CustomMenuItem(suggestionLabel, true);
                    item.setOnAction(e ->{
                        SearchSubsBar.setText(subNickname);
                        suggestionsPopup.hide();
                        try {
                            addSubsTogGridPane(subId);
                        } catch (IOException | SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    suggestionsPopup.getItems().add(item);
                }

                if (!SearchSubsBar.getText().isEmpty() && SearchSubsBar.getScene() != null && SearchSubsBar.getScene().getWindow() != null) {
                    suggestionsPopup.show(SearchSubsBar, Side.BOTTOM, 0, 0);
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

    private void addSubsTogGridPane(Integer subId) throws IOException, SQLException {
        //check for not re-inserting the same users
        for(var pair : subInfoVector) {
            if(pair.getValue().getComponentSubId() == subId) {
                System.out.println("equals ids");
                return;
            }
        }

        ArrayList<Integer> wrapSubId= new ArrayList<>();
        wrapSubId.add(subId);
        Object[] wrapSubInfo = (communityService.getSubscribedData(wrapSubId))[0];
        subInfoVector.addFirst(loadUserInfoComponent(wrapSubInfo,subId));
        if (subInfoVector.size() == allpane.size()+1) {
            subInfoVector.removeLast();
        }
        buildGrid();
    }

    private void showFilteredSubs(String searchTerm) {
        currentSearchTerm = searchTerm;
        //postsContainer.getChildren().clear();
        allPostsLoaded = false;

        Task<Map<Integer,String>> task = new Task<>() {
            @Override
            protected Map<Integer,String> call() {
                return communityService.getFilteredSubs(searchTerm);
            }
        };

        task.setOnSucceeded(event -> {
            Map<Integer,String> filteredSubs = task.getValue();
            //loadPosts(filteredPosts);

            // Se non ci sono risultati, mostra un messaggio
            if (filteredSubs.isEmpty()) {
                Label noResults = new Label("No Subs found for: " + searchTerm);
                //postsContainer.getChildren().add(noResults);
            }
        });
        task.setOnFailed(event -> {
            System.err.println("Failed to load filtered posts");
        });

        new Thread(task).start();
    }

    private void removeFromGrid() {
        subInfoVector.remove(deletingPair);
        deletingPair = null;
        try {
            ArrayList<Integer> wrapSubId = communityService.getSubs(1);
            if(wrapSubId.isEmpty()) {
                buildGrid();
                return;
            }
            Object[][] wrapSubInfo = communityService.getSubscribedData(wrapSubId);
            int subId = wrapSubId.getFirst();
            Object[] subInfo = wrapSubInfo[0];
            subInfoVector.addLast(loadUserInfoComponent(subInfo,subId));

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        buildGrid();
    }

    private void buildGrid() {
        clearGrid();
        for (int i = 0; i < subInfoVector.size() && i < allpane.size(); i++) {
            AnchorPane pane = subInfoVector.get(i).getKey();
            allpane.get(i).getChildren().add(pane);
        }
    }

    private void clearGrid() {
        for (var paneIndex : allpane.keySet()) {
            allpane.get(paneIndex).getChildren().clear();
        }
    }

    private Pair<AnchorPane, UserInfoComponentController> loadUserInfoComponent(Object[] subInfo,int subId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/view/fxml/UserInfoComponent.fxml"));
        UserInfoComponentController userInfoController = new UserInfoComponentController();
        loader.setController(userInfoController);
        AnchorPane pane = loader.load();
        String subNickname = (String)subInfo[0];
        FormattedTime formattedTime = new FormattedTime();
        String spentTime = formattedTime.getFormattedTime (LocalDateTime.parse((String)subInfo[1]));
        userInfoController.setData(subNickname,spentTime);
        userInfoController.setSubId(subId);
        userInfoController.setCommunityService(communityService);
        return new Pair<>(pane, userInfoController);
    }

    public void showDismissButton() {
        PromoteButton.setVisible(false);
        PromoteButton.setManaged(false);
        DismissButton.setVisible(true);
        DismissButton.setManaged(true);
    }

    public void showPromoteButton() {
        DismissButton.setVisible(false);
        DismissButton.setManaged(false);
        PromoteButton.setVisible(true);
        PromoteButton.setManaged(true);
    }

    public void setSubscribedId(int subscriberId){
        this.subscriberId = subscriberId;
    }

    public void setPromoteText(String text) {
        PromoteButton.setText("Promote "+ text);
    }

    public void setDismissText(String text){
         DismissButton.setText("Dismiss "+ text);
    }

    public void setDeletingPair(Pair<AnchorPane, UserInfoComponentController> deletingPair) {
        this.deletingPair = deletingPair;
    }

}
