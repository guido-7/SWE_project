package src.controllers.helpers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import src.businesslogic.CommunityService;
import src.domainmodel.Community;
import src.domainmodel.Role;
import src.servicemanager.GuestContext;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommunitySearchHelper {
    private final TextField searchField;
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private final Consumer<Community> onCommunitySelected;
    private final Function<String, List<Community>> searchFunction;
    private boolean isCommunitySelected = false;

    public CommunitySearchHelper(TextField searchField, Function<String, List<Community>> searchFunction,
                                 Consumer<Community> onCommunitySelected) {
        this.searchField = searchField;
        this.searchFunction = searchFunction;
        this.onCommunitySelected = onCommunitySelected;
    }

    public void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty() || isCommunitySelected) {
                suggestionsPopup.hide();
            } else {
                startSearch(newValue);
            }
        });
    }

    private void startSearch(String query) {
        Task<List<Community>> searchTask = new Task<>() {
            @Override
            protected List<Community> call() {
                return searchFunction.apply(query);// action for search
            }
        };
        searchField.setOnMouseClicked(e -> {
            isCommunitySelected = false;
            searchField.setEditable(true);
        });

        searchTask.setOnSucceeded(event -> {
            List<Community> communities = searchTask.getValue();
            suggestionsPopup.getItems().clear();

            if (communities != null && !communities.isEmpty()) {
                for (Community community : communities) {
                    CommunityService communityService = new CommunityService(community.getId());
                    boolean isSubscribed = communityService.isSubscribed();
                    boolean isGuest = GuestContext.getCurrentGuest().getRole() == Role.GUEST;

                    Text communityText = new Text(community.getTitle());
                    if (!isSubscribed) {
                        communityText.setFill(Color.GRAY); // Se non è iscritto, testo grigio
                    }

                    HBox suggestionBox = new HBox(10, communityText);
                    suggestionBox.prefWidthProperty().bind(searchField.widthProperty());
                    CustomMenuItem item = new CustomMenuItem(suggestionBox, true);
                    item.setHideOnClick(false);

                    item.setOnAction(e -> {
                        isCommunitySelected = true;
                        suggestionsPopup.hide();
                        onCommunitySelected.accept(community);
                    });
                    suggestionsPopup.getItems().add(item);
                }

                Platform.runLater(() -> {
                    if (searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                        suggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
                    }
                });
            }
        });

        new Thread(searchTask).start();
    }
}