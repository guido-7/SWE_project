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
    private final Consumer<Community> onSubscribe;
    private final Function<String, List<Community>> searchFunction;
    private boolean isOverButton = false;
    private boolean isCommunitySelected = false;

    public CommunitySearchHelper(TextField searchField, Function<String, List<Community>> searchFunction,
                                 Consumer<Community> onCommunitySelected, Consumer<Community> onSubscribe) {
        this.searchField = searchField;
        this.searchFunction = searchFunction;
        this.onCommunitySelected = onCommunitySelected;
        this.onSubscribe = onSubscribe;
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
        searchField.setOnMouseClicked(e->{
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


                    // Label con nome community
                    Text communityText = new Text(community.getTitle());
                    if (!isSubscribed) {
                        communityText.setFill(Color.GRAY); // Se non è iscritto, testo grigio
                    }

                    // Bottone "Iscriviti" solo se non iscritto
                    Button subscribeButton = new Button("Subscribe");
                    subscribeButton.setVisible(!isSubscribed && !isGuest && !(onSubscribe == null));
                    subscribeButton.setOnMouseEntered(e->isOverButton = true);
                    subscribeButton.setOnMouseExited(e->isOverButton = false);

                    subscribeButton.setOnMouseClicked(e -> {
                        onSubscribe.accept(community);
                        e.consume();
                        subscribeButton.setVisible(false);
                        communityText.setFill(Color.BLACK);
                    });


                    // Contenitore con testo e bottone
                    HBox suggestionBox = new HBox(10, communityText, subscribeButton);
                    suggestionBox.prefWidthProperty().bind(searchField.widthProperty());// Larghezza dinamica

                    CustomMenuItem item = new CustomMenuItem(suggestionBox, true);
                    item.setHideOnClick(false);


                    item.setOnAction(e -> {
                            if(!isOverButton){
                            //searchField.setText(community.getTitle());
                            isCommunitySelected = true;
                            suggestionsPopup.hide();
                            onCommunitySelected.accept(community);}
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


